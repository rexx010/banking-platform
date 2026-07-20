package com.bankplatform.account.domain.model;

import com.bankplatform.shared.domain.Money;
import com.bankplatform.shared.exceptions.BankException;
import com.bankplatform.shared.exceptions.ErrorCode;
import com.bankplatform.shared.util.IdGenerator;

import java.time.Instant;

/**
 * Account domain entity — aggregate root for account-service.
 *
 * Owns all business rules:
 *   - Cannot debit a suspended or closed account
 *   - Cannot debit more than available balance
 *   - Cannot close an account with a non-zero balance
 *   - Balance is always non-negative
 *
 * Pure Java — no Spring, no JPA, no framework imports.
 * Fully testable without any infrastructure.
 */
public class Account {

    private final String        id;
    private final AccountNumber accountNumber;   // NUBAN
    private final BankCode      bankCode;
    private final String        ownerBvn;
    private final AccountType   accountType;
    private final String        currency;
    private       Money         balance;
    private       AccountStatus status;
    private       long          version;         // optimistic locking
    private final Instant       createdAt;
    private       Instant       updatedAt;

    // Static factory

    /**
     * Opens a new account with zero balance.
     * Status starts as PENDING_KYC if KYC is not yet verified,
     * or ACTIVE if opening via a pre-verified BVN.
     */
    public static Account open(
            AccountNumber accountNumber,
            BankCode      bankCode,
            String        ownerBvn,
            AccountType   accountType,
            String        currency,
            boolean       kycVerified
    ) {
        return new Account(
                IdGenerator.generate(),
                accountNumber,
                bankCode,
                ownerBvn,
                accountType,
                currency,
                Money.of(0L, currency),
                kycVerified ? AccountStatus.ACTIVE : AccountStatus.PENDING_KYC,
                0L,
                Instant.now()
        );
    }

    private Account(
            String id, AccountNumber accountNumber, BankCode bankCode,
            String ownerBvn, AccountType accountType, String currency,
            Money balance, AccountStatus status, long version, Instant createdAt
    ) {
        this.id            = id;
        this.accountNumber = accountNumber;
        this.bankCode      = bankCode;
        this.ownerBvn      = ownerBvn;
        this.accountType   = accountType;
        this.currency      = currency;
        this.balance       = balance;
        this.status        = status;
        this.version       = version;
        this.createdAt     = createdAt;
        this.updatedAt     = createdAt;
    }

    // Domain behaviour

    /**
     * Debits money from this account.
     * Enforces: account must be ACTIVE, balance must be sufficient.
     *
     * Called by transfer-service via DebitAccountUseCase.
     * Never called directly from a controller.
     */
    public void debit(Money amount) {
        assertActive();
        if (balance.isLessThan(amount)) {
            throw new BankException(
                    ErrorCode.TRANSFER_INSUFFICIENT_FUNDS,
                    "Insufficient funds: balance %s, requested %s"
                            .formatted(balance.toMajorUnits(), amount.toMajorUnits())
            );
        }
        this.balance   = balance.subtract(amount);
        this.updatedAt = Instant.now();
    }

    /**
     * Credits money to this account.
     * Credits are allowed even on DORMANT accounts
     * (receiving money reactivates dormant status effectively).
     */
    public void credit(Money amount) {
        if (status == AccountStatus.SUSPENDED ||
                status == AccountStatus.CLOSED) {
            throw new BankException(
                    ErrorCode.ACCOUNT_SUSPENDED,
                    "Cannot credit a " + status.name().toLowerCase() + " account"
            );
        }
        this.balance   = balance.add(amount);
        this.updatedAt = Instant.now();
    }

    /**
     * Activates a PENDING_KYC account once KYC is verified.
     * Called when KycVerifiedEvent arrives from identity-service.
     */
    public void activateAfterKyc() {
        if (status != AccountStatus.PENDING_KYC) {
            return; // already active or other status — no-op
        }
        this.status    = AccountStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void suspend(String reason) {
        if (status == AccountStatus.CLOSED) {
            throw new BankException(ErrorCode.ACCOUNT_CLOSED,
                    "Cannot suspend a closed account");
        }
        this.status    = AccountStatus.SUSPENDED;
        this.updatedAt = Instant.now();
    }

    public void close() {
        if (!balance.isZero()) {
            throw new BankException(ErrorCode.VALIDATION_FAILED,
                    "Cannot close account with non-zero balance");
        }
        this.status    = AccountStatus.CLOSED;
        this.updatedAt = Instant.now();
    }

    // Domain queries

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public boolean canTransact() {
        return status == AccountStatus.ACTIVE;
    }

    // Private helpers

    private void assertActive() {
        switch (status) {
            case SUSPENDED   -> throw new BankException(ErrorCode.ACCOUNT_SUSPENDED);
            case CLOSED      -> throw new BankException(ErrorCode.ACCOUNT_CLOSED);
            case DORMANT     -> throw new BankException(ErrorCode.ACCOUNT_DORMANT);
            case PENDING_KYC -> throw new BankException(ErrorCode.KYC_NOT_VERIFIED,
                    "Account awaiting KYC verification");
            case ACTIVE      -> { /* allowed */ }
        }
    }

    // Getters

    public String        getId()            { return id; }
    public AccountNumber getAccountNumber() { return accountNumber; }
    public BankCode      getBankCode()      { return bankCode; }
    public String        getOwnerBvn()      { return ownerBvn; }
    public AccountType   getAccountType()   { return accountType; }
    public String        getCurrency()      { return currency; }
    public Money         getBalance()       { return balance; }
    public AccountStatus getStatus()        { return status; }
    public long          getVersion()       { return version; }
    public Instant       getCreatedAt()     { return createdAt; }
    public Instant       getUpdatedAt()     { return updatedAt; }
}