package com.bankplatform.account.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name    = "accounts",
        indexes = {
                @Index(name = "idx_accounts_number",
                        columnList = "account_number", unique = true),
                @Index(name = "idx_accounts_owner_bvn",
                        columnList = "owner_bvn")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountJpaEntity {

    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "account_number",
            nullable = false, unique = true, length = 10)
    private String accountNumber;

    @Column(name = "bank_code",
            nullable = false, length = 3)
    private String bankCode;

    @Column(name = "owner_bvn",
            nullable = false, length = 11)
    private String ownerBvn;

    @Column(name = "account_type",
            nullable = false, length = 20)
    private String accountType;

    @Column(name = "currency",
            nullable = false, length = 3)
    private String currency;

    @Column(name = "balance_kobo", nullable = false)
    private long balanceKobo;

    @Column(name = "status",
            nullable = false, length = 20)
    private String status;

    /**
     * Optimistic locking — prevents concurrent double-spend.
     * Hibernate automatically increments this on every UPDATE.
     * If two threads try to update simultaneously, the second
     * gets OptimisticLockException and must retry.
     */
    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @CreationTimestamp
    @Column(name = "created_at",
            nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}