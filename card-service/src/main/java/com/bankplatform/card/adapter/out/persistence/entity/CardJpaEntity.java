package com.bankplatform.card.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name    = "cards",
        indexes = {
                @Index(name = "idx_cards_number",
                        columnList = "card_number", unique = true),
                @Index(name = "idx_cards_nuban",
                        columnList = "linked_nuban", unique = true),
                @Index(name = "idx_cards_owner",
                        columnList = "owner_user_id")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CardJpaEntity {

    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "card_number",
            nullable = false, unique = true, length = 16)
    private String cardNumber;

    @Column(name = "linked_nuban",
            nullable = false, unique = true, length = 10)
    private String linkedNuban;

    @Column(name = "owner_user_id",
            nullable = false, length = 36)
    private String ownerUserId;

    @Column(name = "card_network",
            nullable = false, length = 20)
    private String cardNetwork;

    @Column(name = "expiry_year",  nullable = false)
    private int expiryYear;

    @Column(name = "expiry_month", nullable = false)
    private int expiryMonth;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    // BCrypt hash of card PIN — never plain text
    // CVV is NOT stored — computed on demand via HMAC
    @Column(name = "card_pin_hash", length = 255)
    private String cardPinHash;

    @Column(name = "spending_limit_kobo",
            nullable = false)
    private long spendingLimitKobo;

    @CreationTimestamp
    @Column(name = "issued_at",
            nullable = false, updatable = false)
    private Instant issuedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}