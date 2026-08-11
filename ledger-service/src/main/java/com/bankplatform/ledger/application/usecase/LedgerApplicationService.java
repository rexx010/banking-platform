package com.bankplatform.ledger.application.usecase;

import com.bankplatform.ledger.domain.model.LedgerEntry;
import com.bankplatform.ledger.domain.model.LedgerTransaction;
import com.bankplatform.ledger.domain.port.in.GetLedgerUseCase;
import com.bankplatform.ledger.domain.port.in.RecordTransactionUseCase;
import com.bankplatform.ledger.domain.port.out.LedgerEntryRepository;
import com.bankplatform.shared.domain.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LedgerApplicationService
    implements RecordTransactionUseCase, GetLedgerUseCase {

    private final LedgerEntryRepository repository;

//    Record Transaction
    @Override
    public LedgerTransaction record(LedgerCommands.RecordTransactionCommand command) {
        // Idempotency check — Kafka may deliver the same event twice
        if (repository.existsByReference(command.reference())) {
            log.info("Duplicate ledger entry detected ref={} — skipping",
                    command.reference());
            // Return a reconstructed view rather than failing
            throw new IllegalStateException(
                    "Transaction already recorded: " + command.reference()
            );
        }

        Money amount = Money.of(command.amountKobo(), command.currency());

        LedgerTransaction transaction = LedgerTransaction.record(
                command.reference(),
                command.sourceAccountNumber(),
                command.destAccountNumber(),
                amount,
                command.description()
        );

        // Save both entries atomically — both succeed or neither does
        repository.saveAll(transaction.entries());

        log.info("Ledger entries recorded ref={} source={} dest={} amount={}",
                command.reference(),
                command.sourceAccountNumber(),
                command.destAccountNumber(),
                amount.toMajorUnits()
        );

        return transaction;
    }

    // ── Read operations ───────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<LedgerEntry> getStatement(
            String accountNumber, Instant from, Instant to, Pageable pageable
    ) {
        return repository.findByAccountNumber(
                accountNumber, from, to, pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public long calculateBalanceKobo(String accountNumber) {
        long credits = repository.sumCreditsByAccountNumber(accountNumber);
        long debits  = repository.sumDebitsByAccountNumber(accountNumber);
        return credits - debits;
    }
}
