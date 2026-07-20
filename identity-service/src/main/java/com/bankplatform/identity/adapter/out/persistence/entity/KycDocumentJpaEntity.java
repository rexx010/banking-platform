package com.bankplatform.identity.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "kyc_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycDocumentJpaEntity {

    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;

    /**
     * Many documents belong to one customer.
     * @ManyToOne is the owning side — this table has customer_id column.
     * FetchType.LAZY means the customer is not loaded when fetching documents,
     * which prevents unnecessary database queries.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerJpaEntity customer;

    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType;

    @Column(name = "storage_object_key", nullable = false, length = 500)
    private String storageObjectKey;

    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_size_bytes")
    private long fileSizeBytes;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;
}