package com.bankplatform.ledger.domain.port.out;

import com.bankplatform.ledger.domain.model.LedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

/**
 * OUT-PORT: append-only ledger persistence.
 *
 * Deliberately has no update() or delete() methods.
 * The ledger is an immutable audit trail.
 * Corrections are made by adding new entries, never modifying old ones.
 */
public interface LedgerEntryRepository {

    /** Saves a single entry — the only write operation. */
    LedgerEntry save(LedgerEntry entry);

    /** Saves both entries of a double-entry transaction atomically. */
    List<LedgerEntry> saveAll(List<LedgerEntry> entries);

    /**
     * Checks if a transaction reference has already been recorded.
     * Used for idempotency — Kafka may deliver the same event twice.
     */
    boolean existsByReference(String transactionReference);

    /** Returns all entries for an account in a date range. */
    Page<LedgerEntry> findByAccountNumber(
            String   accountNumber,
            Instant  from,
            Instant  to,
            Pageable pageable
    );

    /** Sums credit amounts for an account — used for balance calculation. */
    long sumCreditsByAccountNumber(String accountNumber);

    /** Sums debit amounts for an account — used for balance calculation. */
    long sumDebitsByAccountNumber(String accountNumber);
}