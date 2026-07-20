package com.bankplatform.identity.adapter.out.storage;

import com.bankplatform.identity.domain.port.out.DocumentStoragePort;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioDocumentStorageAdapter implements DocumentStoragePort {

    private final MinioClient minioClient;

    @Override
    public String upload(
            String bucket, String objectKey,
            InputStream content, long sizeBytes, String contentType
    ) {
        try {
            ensureBucketExists(bucket);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(content, sizeBytes, -1)
                            .contentType(contentType)
                            .build()
            );

            log.info("Uploaded document bucket={} key={} size={}B",
                    bucket, objectKey, sizeBytes);
            return objectKey;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to upload to MinIO: " + e.getMessage(), e
            );
        }
    }

    @Override
    public String getPresignedUrl(
            String bucket, String objectKey, int expiryMinutes
    ) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(expiryMinutes, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to generate pre-signed URL: " + e.getMessage(), e
            );
        }
    }

    @Override
    public void delete(String bucket, String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
            log.info("Deleted document bucket={} key={}", bucket, objectKey);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to delete from MinIO: " + e.getMessage(), e
            );
        }
    }

    private void ensureBucketExists(String bucket) throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucket).build()
        );
        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucket).build()
            );
            log.info("Created MinIO bucket: {}", bucket);
        }
    }
}