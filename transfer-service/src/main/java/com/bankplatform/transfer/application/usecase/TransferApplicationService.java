package com.bankplatform.transfer.application.usecase;

import com.bankplatform.shared.exceptions.BankException;
import com.bankplatform.shared.exceptions.ErrorCode;
import com.bankplatform.shared.logging.MaskingUtil;
import com.bankplatform.transfer.domain.model.Transfer;
import com.bankplatform.transfer.domain.port.in.GetTransferUseCase;
import com.bankplatform.transfer.domain.port.in.InitiateTransferUseCase;
import com.bankplatform.transfer.domain.port.out.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implements the transfer SAGA.
 *
 * SAGA steps:
 *   1. Check idempotency key — return cached result if duplicate
 *   2. Validate source and destination accounts
 *   3. Check transfer limits
 *   4. Save transfer as PENDING
 *   5. Debit source account via account-service
 *   6. Mark transfer as PROCESSING
 *   7. Credit destination account via account-service
 *   8. Mark transfer as COMPLETED → publish TransferCompletedEvent
 *
 * Compensation (if step 7 fails):
 *   C1. Re-credit source account (reverse the debit)
 *   C2. Mark transfer as REVERSED → publish TransferReversedEvent
 *
 * Idempotency:
 *   Each step stores intermediate state in the database.
 *   If the service crashes and restarts, the SAGA can be
 *   resumed or compensated based on the last saved state.
 */
@Slf4j
@Service
@Transactional
public class TransferApplicationService
    implements InitiateTransferUseCase, GetTransferUseCase {

    private final TransferRepository transferRepository;
    private final AccountOperationsPort accountOperations;
    private final TransferEventPublisher eventPublisher;
    private final IdempotencyStore idempotencyStore;
    private final PinVerificationPort pinVerification;
    private final long dailyLimitKobo;
    private final long singleLimitKobo;

    public TransferApplicationService(
            TransferRepository     transferRepository,
            AccountOperationsPort  accountOperations,
            TransferEventPublisher eventPublisher,
            IdempotencyStore       idempotencyStore,
            PinVerificationPort    pinVerification,
            @Value("${transfer.daily-limit-kobo}")  long dailyLimitKobo,
            @Value("${transfer.single-limit-kobo}") long singleLimitKobo
    ) {
        this.transferRepository = transferRepository;
        this.accountOperations  = accountOperations;
        this.eventPublisher     = eventPublisher;
        this.idempotencyStore   = idempotencyStore;
        this.pinVerification    = pinVerification;
        this.dailyLimitKobo     = dailyLimitKobo;
        this.singleLimitKobo    = singleLimitKobo;
    }

    // ── Initiate Transfer (SAGA orchestration) ────────────

    @Override
    public Transfer initiate(TransferCommands.InitiateTransferCommand command) {
        // STEP 0: Verify transaction PIN — before any other work.
        // If the PIN is wrong, we stop immediately.
        // Money never moves without a valid PIN.
        pinVerification.verifyOrThrow(
                command.initiatedByUserId(),
                command.transactionPin()
        );

        log.info("PIN verified for userId={}", command.initiatedByUserId());
        // STEP 1: Idempotency check
        var existingId = idempotencyStore.findTransferId(
                command.idempotencyKey());

        if (existingId.isPresent()) {
            log.info("Duplicate transfer request detected key={} — returning cached result",
                    command.idempotencyKey());
            return transferRepository.findById(existingId.get())
                    .orElseThrow(() ->
                            new BankException(ErrorCode.TRANSFER_NOT_FOUND));
        }

        // STEP 2: Validate accounts
        if (command.sourceAccountNumber()
                .equals(command.destinationAccountNumber())) {
            throw new BankException(ErrorCode.TRANSFER_SAME_ACCOUNT);
        }

        if (!accountOperations.isAccountActive(
                command.destinationAccountNumber())) {
            throw new BankException(ErrorCode.ACCOUNT_NOT_FOUND,
                    "Destination account is not active");
        }

        // STEP 3: Check single transfer limit
        if (command.amountKobo() > singleLimitKobo) {
            throw new BankException(ErrorCode.TRANSFER_LIMIT_EXCEEDED,
                    "Transfer amount exceeds single transfer limit");
        }

        // STEP 4: Create and save transfer as PENDING
        Transfer transfer = Transfer.initiate(
                command.idempotencyKey(),
                command.sourceAccountNumber(),
                command.destinationAccountNumber(),
                command.destinationBankCode(),
                command.amountKobo(),
                command.currency(),
                command.narration(),
                command.initiatedByUserId()
        );
        transfer = transferRepository.save(transfer);

        // Store idempotency key so duplicates return this transfer
        idempotencyStore.store(command.idempotencyKey(), transfer.getId());

        MDC.put("transferId", transfer.getId());

        log.info("Transfer initiated transferId={} amount={} src={} dest={}",
                transfer.getId(),
                transfer.getAmountKobo(),
                MaskingUtil.maskNuban(transfer.getSourceAccountNumber()),
                MaskingUtil.maskNuban(transfer.getDestinationAccountNumber())
        );

        eventPublisher.publishInitiated(transfer);

        // STEP 5: Debit source account
        try {
            accountOperations.debit(
                    transfer.getSourceAccountNumber(),
                    transfer.getAmountKobo(),
                    transfer.getCurrency(),
                    transfer.getId()
            );
        } catch (Exception ex) {
            // Debit failed — transfer failed before any money moved
            transfer.fail(ex.getMessage());
            transfer = transferRepository.save(transfer);
            eventPublisher.publishFailed(transfer);
            log.warn("Transfer failed at debit step transferId={}: {}",
                    transfer.getId(), ex.getMessage());
            throw ex;
        }

        // STEP 6: Mark as PROCESSING (debit done, credit pending)
        transfer.markProcessing();
        transfer = transferRepository.save(transfer);

        log.info("Source debited transferId={}", transfer.getId());

        // STEP 7: Credit destination account
        try {
            accountOperations.credit(
                    transfer.getDestinationAccountNumber(),
                    transfer.getAmountKobo(),
                    transfer.getCurrency(),
                    transfer.getId()
            );
        } catch (Exception ex) {
            // COMPENSATION: credit failed — reverse the debit
            log.error(
                    "Credit failed transferId={} — initiating reversal: {}",
                    transfer.getId(), ex.getMessage());

            try {
                accountOperations.credit(
                        transfer.getSourceAccountNumber(),
                        transfer.getAmountKobo(),
                        transfer.getCurrency(),
                        transfer.getId() + "_REVERSAL"
                );
                transfer.reverse("Credit failed: " + ex.getMessage());
                transfer = transferRepository.save(transfer);
                eventPublisher.publishReversed(transfer);
                log.info("Transfer reversed transferId={}", transfer.getId());
            } catch (Exception reversalEx) {
                // Reversal also failed — critical alert needed
                log.error(
                        "CRITICAL: reversal failed transferId={}: {}",
                        transfer.getId(), reversalEx.getMessage());
                // Mark as failed — manual intervention required
                transfer.fail("Reversal failed: " + reversalEx.getMessage());
                transferRepository.save(transfer);
            }

            throw new BankException(ErrorCode.TRANSFER_FAILED,
                    "Transfer could not be completed — funds returned if debited");
        }

        // STEP 8: Complete the transfer
        transfer.complete();
        transfer = transferRepository.save(transfer);
        eventPublisher.publishCompleted(transfer);

        log.info("Transfer completed transferId={}", transfer.getId());
        return transfer;
    }

    // ── Read operations ───────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Transfer getById(String transferId) {
        return transferRepository.findById(transferId)
                .orElseThrow(() ->
                        new BankException(ErrorCode.TRANSFER_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transfer> getByAccountNumber(String accountNumber) {
        return transferRepository
                .findBySourceAccountNumber(accountNumber);
    }
}
