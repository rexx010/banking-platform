package com.bankplatform.identity.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name    = "customers",
        indexes = {
                @Index(name = "idx_customers_bvn",         columnList = "bvn",         unique = true),
                @Index(name = "idx_customers_nin",         columnList = "nin",         unique = true),
                @Index(name = "idx_customers_auth_user_id",columnList = "auth_user_id",unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerJpaEntity {

    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "auth_user_id", nullable = false, unique = true, length = 36)
    private String authUserId;

    @Column(name = "bvn", nullable = false, unique = true, length = 11)
    private String bvn;

    @Column(name = "nin", unique = true, length = 11)
    private String nin;

    @Column(name = "first_name",  nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name",   nullable = false, length = 100)
    private String lastName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "state_of_origin", length = 100)
    private String stateOfOrigin;

    @Column(name = "kyc_status", nullable = false, length = 30)
    private String kycStatus;

    @Column(name = "kyc_rejection_reason", length = 500)
    private String kycRejectionReason;

    /**
     * One customer has many KYC documents.
     * CascadeType.ALL: operations on customer cascade to documents.
     * orphanRemoval: removing a doc from the list deletes it from DB.
     * FetchType.LAZY: documents are only loaded when explicitly accessed,
     * not on every customer query — important for performance.
     */
    @OneToMany(
            mappedBy      = "customer",
            cascade       = CascadeType.ALL,
            orphanRemoval = true,
            fetch         = FetchType.LAZY
    )
    @Builder.Default
    private List<KycDocumentJpaEntity> documents = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}