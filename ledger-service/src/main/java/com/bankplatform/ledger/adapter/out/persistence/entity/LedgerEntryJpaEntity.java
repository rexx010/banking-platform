package com.bankplatform.ledger.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * JPA entity for ledger_entries table.
 *
 * All columns are updatable=false because ledger entries
 * are immutable after creation. This is enforced at two levels:
 *   1. Domain: LedgerEntry is a record (immutable by definition)
 *   2. Database: updatable=false prevents accidental UPDATEs
 */
@Entity
@Table(
        name    = "ledger_entries",
        indexes = {
                @Index(name = "idx_ledger_account",
                        columnList = "account_number"),
                @Index(name = "idx_ledger_reference",
                        columnList = "transaction_reference"),
                @Index(name = "idx_ledger_created",
                        columnList = "created_at")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerEntryJpaEntity {

    @Id
    @Column(name = "id",
            columnDefinition = "VARCHAR(36)",
            updatable = false)
    private String id;

    @Column(name = "transaction_reference",
            nullable = false, updatable = false)
    private String transactionReference;

    @Column(name = "account_number",
            nullable = false, length = 10, updatable = false)
    private String accountNumber;

    @Column(name = "entry_type",
            nullable = false, length = 10, updatable = false)
    private String entryType;   // DEBIT or CREDIT

    @Column(name = "amount_kobo",
            nullable = false, updatable = false)
    private long amountKobo;

    @Column(name = "currency",
            nullable = false, length = 3, updatable = false)
    private String currency;

    @Column(name = "description",
            nullable = false, length = 500, updatable = false)
    private String description;

    @Column(name = "counterpart_account_number",
            length = 10, updatable = false)
    private String counterpartAccountNumber;

    @Column(name = "created_at",
            nullable = false, updatable = false)
    private Instant createdAt;
}