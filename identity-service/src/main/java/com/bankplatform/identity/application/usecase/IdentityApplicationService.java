package com.bankplatform.identity.application.usecase;

import com.bankplatform.identity.application.usecase.IdentityCommands.*;
import com.bankplatform.identity.domain.model.*;
import com.bankplatform.identity.domain.port.in.*;
import com.bankplatform.identity.domain.port.out.*;
import com.bankplatform.shared.exceptions.BankException;
import com.bankplatform.shared.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class IdentityApplicationService
        implements CreateBvnUseCase, SubmitKycDocumentsUseCase, GetCustomerUseCase {

    private final CustomerRepository     customerRepository;
    private final DocumentStoragePort    documentStorage;
    private final IdentityEventPublisher eventPublisher;
    private final BvnGenerator           bvnGenerator;

    @Value("${minio.buckets.kyc-documents}")
    private String kycBucket;

    //  Create BVN

    @Override
    public Customer createBvn(CreateBvnCommand command) {
        log.info("Creating BVN for authUserId={}", command.authUserId());

        // One BVN per NIN — enforce uniqueness
        if (command.nin() != null
                && customerRepository.existsByNin(command.nin())) {
            throw new BankException(
                    ErrorCode.BVN_ALREADY_EXISTS,
                    "A BVN already exists for this NIN"
            );
        }

        // One BVN per user account
        if (customerRepository.findByAuthUserId(command.authUserId()).isPresent()) {
            throw new BankException(
                    ErrorCode.DUPLICATE_RESOURCE,
                    "This user already has a BVN registered"
            );
        }

        Bvn bvn = bvnGenerator.generate();

        Customer customer = Customer.create(
                command.authUserId(), bvn, command.nin(),
                command.firstName(), command.lastName(), command.middleName(),
                command.dateOfBirth(), command.phoneNumber(), command.email()
        );
        customer.setAddress(command.address());
        customer.setStateOfOrigin(command.stateOfOrigin());

        Customer saved = customerRepository.save(customer);

        eventPublisher.publishBvnCreated(saved);

        log.info("BVN created customerId={} bvn={}",
                saved.getId(), bvn.masked());
        return saved;
    }

    //  Upload Document

    @Override
    public Customer uploadDocument(UploadDocumentCommand command) {
        Customer customer = customerRepository
                .findByAuthUserId(command.authUserId())
                .orElseThrow(() -> new BankException(
                        ErrorCode.BVN_NOT_FOUND,
                        "No BVN found for this user — create BVN first"
                ));

        // Validate file size — max 5MB
        long maxBytes = 5L * 1024 * 1024;
        if (command.fileSizeBytes() > maxBytes) {
            throw new BankException(ErrorCode.KYC_DOCUMENT_TOO_LARGE,
                    "Document exceeds maximum size of 5MB");
        }

        // Build deterministic object key — organised and collision-free
        String objectKey = "kyc/%s/%s/%s".formatted(
                customer.getId(),
                command.documentType().name(),
                command.originalFilename()
                        .replaceAll("[^a-zA-Z0-9._-]", "_")
        );

        log.info("Uploading KYC document type={} customerId={}",
                command.documentType(), customer.getId());

        documentStorage.upload(
                kycBucket, objectKey,
                command.fileContent(),
                command.fileSizeBytes(),
                command.contentType()
        );

        KycDocument doc = KycDocument.of(
                command.documentType(), objectKey,
                command.originalFilename(),
                command.contentType(),
                command.fileSizeBytes()
        );

        customer.addDocument(doc);

        // Auto-submit for review when minimum documents are present
        if (hasRequiredDocuments(customer)
                && customer.getKycStatus() == KycStatus.DOCUMENTS_SUBMITTED) {
            customer.startReview();
            log.info("KYC auto-submitted for review customerId={}",
                    customer.getId());
        }

        Customer saved = customerRepository.save(customer);
        log.info("Document uploaded customerId={} status={}",
                customer.getId(), saved.getKycStatus());
        return saved;
    }

    //  Get Customer

    @Override
    @Transactional(readOnly = true)
    public Customer getByBvn(String bvn) {
        return customerRepository.findByBvn(bvn)
                .orElseThrow(() -> new BankException(ErrorCode.BVN_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getByAuthUserId(String authUserId) {
        return customerRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BankException(
                        ErrorCode.BVN_NOT_FOUND,
                        "No identity profile found for this user"
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findByBvn(String bvn) {
        return customerRepository.findByBvn(bvn);
    }

    //  Private helpers

    /**
     * Minimum KYC: at least one government photo ID + one selfie.
     * This rule lives here so it is easy to update as regulations change.
     */
    private boolean hasRequiredDocuments(Customer customer) {
        var types = customer.getDocuments().stream()
                .map(KycDocument::documentType)
                .toList();

        boolean hasGovId = types.stream().anyMatch(t ->
                t == DocumentType.NATIONAL_ID_FRONT      ||
                        t == DocumentType.DRIVERS_LICENSE_FRONT  ||
                        t == DocumentType.INTERNATIONAL_PASSPORT ||
                        t == DocumentType.VOTERS_CARD
        );

        boolean hasSelfie =
                types.contains(DocumentType.LIVE_SELFIE) ||
                        types.contains(DocumentType.SELFIE_WITH_ID);

        return hasGovId && hasSelfie;
    }
}