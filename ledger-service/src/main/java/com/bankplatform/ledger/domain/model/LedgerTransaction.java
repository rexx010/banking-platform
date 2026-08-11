package com.bankplatform.ledger.domain.model;

import com.bankplatform.shared.domain.Money;
import com.bankplatform.shared.exceptions.BankException;
import com.bankplatform.shared.exceptions.ErrorCode;
import com.bankplatform.shared.util.IdGenerator;

import java.time.Instant;
import java.util.List;

/**
 * A complete double-entry transaction — always two entries.
 *
 * Enforces the fundamental accounting invariant:
 *   total DEBIT amount == total CREDIT amount
 *
 * If this invariant is violated, an exception is thrown.
 * Money cannot be created or destroyed in this system.
 */
public record LedgerTransaction(
        String           reference,        // unique transaction ID from transfer-service
        String           description,
        LedgerEntry      debitEntry,
        LedgerEntry      creditEntry,
        Instant          recordedAt
) {

    public static LedgerTransaction record(
            String reference,
            String sourceAccount,
            String destAccount,
            Money  amount,
            String description
    ) {
        LedgerEntry debit = LedgerEntry.create(
                reference, sourceAccount,
                EntryType.DEBIT, amount,
                description, destAccount
        );

        LedgerEntry credit = LedgerEntry.create(
                reference, destAccount,
                EntryType.CREDIT, amount,
                description, sourceAccount
        );

        LedgerTransaction transaction = new LedgerTransaction(
                reference, description, debit, credit, Instant.now()
        );

        // Validate the double-entry invariant
        transaction.validate();

        return transaction;
    }

    /**
     * Validates that debit amount equals credit amount.
     * This is the core double-entry bookkeeping rule.
     * If this throws, there is a bug in the calling code.
     */
    private void validate() {
        if (!debitEntry.amount().equals(creditEntry.amount())) {
            throw new BankException(
                    ErrorCode.INTERNAL_ERROR,
                    "Double-entry violated: debit %s != credit %s"
                            .formatted(
                                    debitEntry.amount().toMajorUnits(),
                                    creditEntry.amount().toMajorUnits()
                            )
            );
        }
    }

    public List<LedgerEntry> entries() {
        return List.of(debitEntry, creditEntry);
    }
}