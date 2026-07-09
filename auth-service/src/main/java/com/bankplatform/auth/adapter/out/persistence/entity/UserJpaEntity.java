package com.bankplatform.auth.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * JPA entity for the users table.
 *
 * This class is only used inside the persistence adapter.
 * The domain never sees this class.
 * The mapper converts between UserJpaEntity and User.
 *
 * @ElementCollection stores roles in a separate join table
 * user_roles(user_id, role) without needing a full @Entity
 * for Role. EAGER fetch means roles are always loaded with
 * the user — we always need them for JWT generation.
 */
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(
                        name = "idx_users_email",
                        columnList = "email",
                        unique = true
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserJpaEntity {
    @Id
    @Column(
            name = "id",
            columnDefinition = "VARCHAR(36)",
            updatable = false,
            nullable = false
    )
    private String id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "transaction_pin_hash")
    private String transactionPinHash;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name        = "user_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "role", length = 50)
    @Builder.Default
    private Set<String> roles = new HashSet<>();

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private int failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @CreationTimestamp
    @Column(
            name      = "created_at",
            nullable  = false,
            updatable = false
    )
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
