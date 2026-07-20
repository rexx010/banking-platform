package com.bankplatform.account.application.usecase;

import com.bankplatform.account.application.usecase.AccountCommands.*;
import com.bankplatform.account.domain.model.*;
import com.bankplatform.account.domain.port.in.*;
import com.bankplatform.account.domain.port.out.*;
import com.bankplatform.shared.domain.Money;
import com.bankplatform.shared.exceptions.BankException;
import com.bankplatform.shared.exceptions.ErrorCode;
import com.bankplatform.shared.logging.MaskingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AccountApplicationService
        implements
        OpenAccountUseCase,
        GetAccountUseCase,
        DebitAccountUseCase,
        CreditAccountUseCase {

    private final AccountRepository    accountRepository;
    private final BvnVerificationPort  bvnVerificationPort;
    private final AccountEventPublisher eventPublisher;
    private final NubanGenerator        nubanGenerator;

    // Open Account

    @Override
    public Account openAccount(OpenAccountCommand command) {
        log.info("Opening {} account for bvn={} bank={}",
                command.accountType(),
                MaskingUtil.maskBvn(command.bvn()),
                command.bankCode());
        // 1. Verify BVN with identity-service
        boolean kycVerified = bvnVerificationPort.isBvnVerified(command.bvn());
        // 2. Generate unique NUBAN
        BankCode      bankCode = new BankCode(command.bankCode());
        AccountNumber nuban    = nubanGenerator.generate(bankCode);

        // 3. Create domain entity
        Account account = Account.open(
                nuban,
                bankCode,
                command.bvn(),
                AccountType.valueOf(command.accountType()),
                command.currency(),
                kycVerified
        );

        // 4. Save
        Account saved = accountRepository.save(account);

        // 5. Publish event
        eventPublisher.publishAccountCreated(saved);
        log.info("Account opened accountId={} nuban={} status={}",
                saved.getId(), nuban.masked(), saved.getStatus());

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Account getByAccountNumber(String accountNumber) {
        return accountRepository
                .findByAccountNumber(new AccountNumber(accountNumber))
                .orElseThrow(() -> new BankException(
                        ErrorCode.ACCOUNT_NOT_FOUND,
                        "Account not found: " + MaskingUtil.maskNuban(accountNumber)
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Account getById(String accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new BankException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getByBvn(String bvn) {
        return accountRepository.findByOwnerBvn(bvn);
    }

    // Debit
    @Override
    public Account debit(DebitCommand command) {
        Account account = accountRepository
                .findByAccountNumber(new AccountNumber(command.accountNumber()))
                .orElseThrow(() -> new BankException(ErrorCode.ACCOUNT_NOT_FOUND));

        Money amount = Money.of(command.amountKobo(), command.currency());
        log.info("Debiting account={} amount={} ref={}",
                account.getAccountNumber().masked(),
                amount.toMajorUnits(),
                command.reference());
        // Domain entity enforces the business rules
        account.debit(amount);
        Account saved = accountRepository.save(account);
        log.info("Debit successful accountId={} newBalance={}",
                saved.getId(), saved.getBalance().toMajorUnits());
        return saved;
    }

    // Credit

    @Override
    public Account credit(CreditCommand command) {
        Account account = accountRepository
                .findByAccountNumber(new AccountNumber(command.accountNumber()))
                .orElseThrow(() -> new BankException(ErrorCode.ACCOUNT_NOT_FOUND));

        Money amount = Money.of(command.amountKobo(), command.currency());

        log.info("Crediting account={} amount={} ref={}",
                account.getAccountNumber().masked(),
                amount.toMajorUnits(),
                command.reference());

        account.credit(amount);

        Account saved = accountRepository.save(account);

        log.info("Credit successful accountId={} newBalance={}",
                saved.getId(), saved.getBalance().toMajorUnits());

        return saved;
    }

    // KYC activation
    /**
     * Called when KycVerifiedEvent arrives from identity-service.
     * Activates all PENDING_KYC accounts for this BVN.
     */
    public void activateAccountsForBvn(String bvn) {
        List<Account> accounts = accountRepository.findByOwnerBvn(bvn);

        accounts.stream()
                .filter(a -> a.getStatus() == AccountStatus.PENDING_KYC)
                .forEach(account -> {
                    String prev = account.getStatus().name();
                    account.activateAfterKyc();
                    accountRepository.save(account);
                    eventPublisher.publishAccountStatusChanged(
                            account, prev, "KYC verified");
                    log.info("Account activated after KYC accountId={}",
                            account.getId());
                });
    }
}