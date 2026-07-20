package com.bankplatform.identity.domain.port.out;

import java.io.InputStream;

/**
 * OUT-PORT: file storage operations.
 * Implemented by MinioDocumentStorageAdapter.
 */
public interface DocumentStoragePort {

    /**
     * Uploads a file and returns the object key.
     * The object key is a permanent identifier for the file.
     * It is NOT a URL — URLs expire. Keys are permanent.
     */
    String upload(
            String      bucket,
            String      objectKey,
            InputStream content,
            long        sizeBytes,
            String      contentType
    );

    /**
     * Generates a time-limited URL to view the file.
     * After expiryMinutes the URL stops working.
     * Never store this URL — generate fresh on each request.
     */
    String getPresignedUrl(
            String bucket,
            String objectKey,
            int    expiryMinutes
    );

    void delete(String bucket, String objectKey);
}