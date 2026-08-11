package com.bankplatform.ledger.domain.port.in;

import com.bankplatform.ledger.domain.model.LedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface GetLedgerUseCase {

    Page<LedgerEntry> getStatement(
            String   accountNumber,
            Instant  from,
            Instant  to,
            Pageable pageable
    );

    /**
     * Calculates the current balance by summing all entries.
     * credits - debits = balance
     *
     * This is the authoritative balance source — the ledger
     * is the source of truth, not the account table balance
     * (which is a cached value for performance).
     */
    long calculateBalanceKobo(String accountNumber);
}