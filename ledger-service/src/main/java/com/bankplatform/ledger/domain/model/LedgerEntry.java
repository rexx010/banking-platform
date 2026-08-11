package com.bankplatform.ledger.domain.model;

import com.bankplatform.shared.domain.Money;
import com.bankplatform.shared.util.IdGenerator;

import java.time.Instant;

public record LedgerEntry(
        String    id,
        String    transactionReference,
        String    accountNumber,
        EntryType entryType,
        Money     amount,
        String    currency,
        String    description,
        String    counterpartAccountNumber,
        Instant   createdAt
) {

    /**
     * Creates a new ledger entry.
     * Called only from LedgerTransaction when recording a transfer.
     */
    public static LedgerEntry create(
            String    transactionReference,
            String    accountNumber,
            EntryType entryType,
            Money     amount,
            String    description,
            String    counterpartAccountNumber
    ) {
        return new LedgerEntry(
                IdGenerator.generate(),
                transactionReference,
                accountNumber,
                entryType,
                amount,
                amount.getCurrency().getCurrencyCode(),
                description,
                counterpartAccountNumber,
                Instant.now()
        );
    }
}