package com.bankplatform.transfer.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name    = "transfers",
        indexes = {
                @Index(name = "idx_transfers_idempotency",
                        columnList = "idempotency_key", unique = true),
                @Index(name = "idx_transfers_source",
                        columnList = "source_account_number"),
                @Index(name = "idx_transfers_status",
                        columnList = "status")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransferJpaEntity {

    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(50)")
    private String id;

    /**
     * Unique key provided by client — prevents duplicate transfers.
     * If a transfer with this key exists, we return it instead
     * of executing a new one.
     */
    @Column(name = "idempotency_key",
            nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Column(name = "source_account_number",
            nullable = false, length = 10)
    private String sourceAccountNumber;

    @Column(name = "destination_account_number",
            nullable = false, length = 10)
    private String destinationAccountNumber;

    @Column(name = "destination_bank_code",
            nullable = false, length = 3)
    private String destinationBankCode;

    /** Always in kobo — never decimal */
    @Column(name = "amount_kobo", nullable = false)
    private long amountKobo;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "narration", length = 200)
    private String narration;

    @Column(name = "initiated_by_user_id",
            nullable = false, length = 36)
    private String initiatedByUserId;

    /**
     * SAGA state — updated on every step.
     * If service crashes, we read this to determine
     * what compensation is needed on restart.
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at",
            nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}