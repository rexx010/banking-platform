package com.bankplatform.identity.application.usecase;

import com.bankplatform.identity.domain.model.DocumentType;

import java.io.InputStream;
import java.time.LocalDate;

public final class IdentityCommands {

    private IdentityCommands() {}

    /**
     * Command to create a BVN and customer identity profile.
     * Called after a user completes the onboarding form.
     */
    public record CreateBvnCommand(
            String    authUserId,
            String    nin,           // optional at creation
            String    firstName,
            String    lastName,
            String    middleName,
            LocalDate dateOfBirth,
            String    phoneNumber,
            String    email,
            String    address,
            String    stateOfOrigin
    ) {}

    /**
     * Command to upload a KYC document.
     *
     * InputStream streams the file directly to MinIO without
     * loading the entire file into heap memory first.
     * Always preferred over byte[] for file uploads.
     */
    public record UploadDocumentCommand(
            String       authUserId,
            DocumentType documentType,
            String       originalFilename,
            String       contentType,
            long         fileSizeBytes,
            InputStream  fileContent
    ) {}
}