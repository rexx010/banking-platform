package com.bankplatform.identity.domain.model;

import com.bankplatform.shared.util.IdGenerator;

import java.time.Instant;

/**
 * Value object representing a single uploaded KYC document.
 *
 * Stores METADATA only — not the actual file bytes.
 * The file itself lives in MinIO at storageObjectKey.
 *
 * Example storageObjectKey:
 * "kyc/customer-abc123/LIVE_SELFIE/selfie_2025.jpg"
 */
public record KycDocument(
        String       id,
        DocumentType documentType,
        String       storageObjectKey,   // permanent MinIO path — NOT a URL
        String       originalFilename,
        String       contentType,        // "image/jpeg", "application/pdf"
        long         fileSizeBytes,
        Instant uploadedAt
) {
    /**
     * Factory method called after successfully uploading to MinIO.
     * Creates the metadata record with a generated ID.
     */
    public static KycDocument of(
            DocumentType documentType,
            String       storageObjectKey,
            String       originalFilename,
            String       contentType,
            long         fileSizeBytes
    ) {
        return new KycDocument(
                IdGenerator.generate(),
                documentType,
                storageObjectKey,
                originalFilename,
                contentType,
                fileSizeBytes,
                Instant.now()
        );
    }
}
