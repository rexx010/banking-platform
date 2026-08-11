package com.bankplatform.ledger.adapter.out.persistence;

import com.bankplatform.ledger.adapter.out.persistence.entity.LedgerEntryJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

interface LedgerJpaRepository
        extends JpaRepository<LedgerEntryJpaEntity, String> {

    boolean existsByTransactionReference(String transactionReference);

    Page<LedgerEntryJpaEntity> findByAccountNumberAndCreatedAtBetween(
            String  accountNumber,
            Instant from,
            Instant to,
            Pageable pageable
    );

    /**
     * Sums all CREDIT amounts for an account.
     * COALESCE returns 0 if no entries exist — prevents NullPointerException.
     */
    @Query("""
        SELECT COALESCE(SUM(e.amountKobo), 0)
        FROM LedgerEntryJpaEntity e
        WHERE e.accountNumber = :accountNumber
        AND   e.entryType = 'CREDIT'
        """)
    long sumCreditsByAccountNumber(@Param("accountNumber") String accountNumber);

    /**
     * Sums all DEBIT amounts for an account.
     */
    @Query("""
        SELECT COALESCE(SUM(e.amountKobo), 0)
        FROM LedgerEntryJpaEntity e
        WHERE e.accountNumber = :accountNumber
        AND   e.entryType = 'DEBIT'
        """)
    long sumDebitsByAccountNumber(@Param("accountNumber") String accountNumber);
}