package com.bankplatform.transfer.domain.model;

import com.bankplatform.shared.exceptions.BankException;
import com.bankplatform.shared.exceptions.ErrorCode;
import com.bankplatform.shared.util.IdGenerator;

import java.time.Instant;

/**
 * Transfer domain entity — represents one money movement.
 *
 * Tracks the full SAGA lifecycle from PENDING to a terminal state.
 * Immutable fields (source, destination, amount) are set at creation.
 * Only status can change — and only in valid directions.
 */
public class Transfer {

    private final String         id;
    private final String         idempotencyKey;   // prevents duplicate transfers
    private final String         sourceAccountNumber;
    private final String         destinationAccountNumber;
    private final String         destinationBankCode;
    private final long           amountKobo;
    private final String         currency;
    private final String         narration;
    private final String         initiatedByUserId;
    private       TransferStatus status;
    private       String         failureReason;
    private final Instant        createdAt;
    private       Instant        updatedAt;
    private       Instant        completedAt;

    // ── Static factory ────────────────────────────────────

    public static Transfer initiate(
            String idempotencyKey,
            String sourceAccountNumber,
            String destinationAccountNumber,
            String destinationBankCode,
            long   amountKobo,
            String currency,
            String narration,
            String initiatedByUserId
    ) {
        return new Transfer(
                IdGenerator.prefixed("txn"),
                idempotencyKey,
                sourceAccountNumber,
                destinationAccountNumber,
                destinationBankCode,
                amountKobo,
                currency,
                narration,
                initiatedByUserId,
                TransferStatus.PENDING,
                null,
                Instant.now()
        );
    }

    private Transfer(
            String id, String idempotencyKey,
            String sourceAccountNumber, String destinationAccountNumber,
            String destinationBankCode, long amountKobo, String currency,
            String narration, String initiatedByUserId,
            TransferStatus status, String failureReason, Instant createdAt
    ) {
        this.id                      = id;
        this.idempotencyKey          = idempotencyKey;
        this.sourceAccountNumber     = sourceAccountNumber;
        this.destinationAccountNumber= destinationAccountNumber;
        this.destinationBankCode     = destinationBankCode;
        this.amountKobo              = amountKobo;
        this.currency                = currency;
        this.narration               = narration;
        this.initiatedByUserId       = initiatedByUserId;
        this.status                  = status;
        this.failureReason           = failureReason;
        this.createdAt               = createdAt;
        this.updatedAt               = createdAt;
    }

    // ── State transitions ─────────────────────────────────

    /** Source account debited — credit in progress */
    public void markProcessing() {
        assertNotTerminal();
        this.status    = TransferStatus.PROCESSING;
        this.updatedAt = Instant.now();
    }

    /** Both debit and credit succeeded */
    public void complete() {
        assertNotTerminal();
        this.status      = TransferStatus.COMPLETED;
        this.updatedAt   = Instant.now();
        this.completedAt = Instant.now();
    }

    /** Transfer failed before any debit was made */
    public void fail(String reason) {
        assertNotTerminal();
        this.status        = TransferStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt     = Instant.now();
    }

    /** Debit was made but credit failed — money returned to source */
    public void reverse(String reason) {
        if (status != TransferStatus.PROCESSING) {
            throw new BankException(ErrorCode.TRANSFER_FAILED,
                    "Can only reverse a PROCESSING transfer");
        }
        this.status        = TransferStatus.REVERSED;
        this.failureReason = reason;
        this.updatedAt     = Instant.now();
    }

    private void assertNotTerminal() {
        if (status == TransferStatus.COMPLETED ||
                status == TransferStatus.FAILED    ||
                status == TransferStatus.REVERSED) {
            throw new BankException(ErrorCode.TRANSFER_FAILED,
                    "Transfer is in terminal state: " + status);
        }
    }

    // ── Getters ───────────────────────────────────────────

    public String         getId()                       { return id; }
    public String         getIdempotencyKey()           { return idempotencyKey; }
    public String         getSourceAccountNumber()      { return sourceAccountNumber; }
    public String         getDestinationAccountNumber() { return destinationAccountNumber; }
    public String         getDestinationBankCode()      { return destinationBankCode; }
    public long           getAmountKobo()               { return amountKobo; }
    public String         getCurrency()                 { return currency; }
    public String         getNarration()                { return narration; }
    public String         getInitiatedByUserId()        { return initiatedByUserId; }
    public TransferStatus getStatus()                   { return status; }
    public String         getFailureReason()            { return failureReason; }
    public Instant        getCreatedAt()                { return createdAt; }
    public Instant        getUpdatedAt()                { return updatedAt; }
    public Instant        getCompletedAt()              { return completedAt; }
}