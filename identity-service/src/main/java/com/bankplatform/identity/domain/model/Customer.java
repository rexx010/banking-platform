package com.bankplatform.identity.domain.model;

import com.bankplatform.shared.exceptions.BankException;
import com.bankplatform.shared.exceptions.ErrorCode;
import com.bankplatform.shared.util.IdGenerator;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Customer domain entity — the aggregate root for identity.
 *
 * Owns all KYC documents and controls all KYC state transitions.
 * Business rules enforced here:
 *   - Cannot add documents after VERIFIED
 *   - Cannot approve KYC unless UNDER_REVIEW
 *   - Cannot reject KYC unless UNDER_REVIEW
 *   - Auto-transitions to DOCUMENTS_SUBMITTED when first doc added
 */
public class Customer {

    private final String     id;
    private final String     authUserId;
    private final Bvn        bvn;
    private final String     nin;
    private final String     firstName;
    private final String     lastName;
    private final String     middleName;
    private final LocalDate  dateOfBirth;
    private final String     phoneNumber;
    private final String     email;
    private       String     address;
    private       String     stateOfOrigin;
    private       KycStatus  kycStatus;
    private       String     kycRejectionReason;
    private final List<KycDocument> documents;
    private final Instant    createdAt;
    private       Instant    updatedAt;

    // ── Static factory ────────────────────────────────────

    public static Customer create(
            String    authUserId,
            Bvn       bvn,
            String    nin,
            String    firstName,
            String    lastName,
            String    middleName,
            LocalDate dateOfBirth,
            String    phoneNumber,
            String    email
    ) {
        return new Customer(
                IdGenerator.generate(),
                authUserId, bvn, nin,
                firstName, lastName, middleName,
                dateOfBirth, phoneNumber, email,
                KycStatus.PENDING,
                new ArrayList<>(),
                Instant.now()
        );
    }

    private Customer(
            String id, String authUserId, Bvn bvn, String nin,
            String firstName, String lastName, String middleName,
            LocalDate dateOfBirth, String phoneNumber, String email,
            KycStatus kycStatus, List<KycDocument> documents,
            Instant createdAt
    ) {
        this.id          = id;
        this.authUserId  = authUserId;
        this.bvn         = bvn;
        this.nin         = nin;
        this.firstName   = firstName;
        this.lastName    = lastName;
        this.middleName  = middleName;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.email       = email;
        this.kycStatus   = kycStatus;
        this.documents   = new ArrayList<>(documents);
        this.createdAt   = createdAt;
        this.updatedAt   = createdAt;
    }

    // ── Domain behaviour ──────────────────────────────────

    /**
     * Records a newly uploaded document.
     * Transitions to DOCUMENTS_SUBMITTED if currently PENDING or REJECTED.
     * Throws if already VERIFIED — verified customers need no more documents.
     */
    public void addDocument(KycDocument document) {
        if (kycStatus == KycStatus.VERIFIED) {
            throw new BankException(
                    ErrorCode.KYC_ALREADY_SUBMITTED,
                    "KYC is already verified — no further documents needed"
            );
        }
        documents.add(document);
        if (kycStatus == KycStatus.PENDING
                || kycStatus == KycStatus.REJECTED) {
            kycStatus = KycStatus.DOCUMENTS_SUBMITTED;
        }
        updatedAt = Instant.now();
    }

    /** Called when enough documents exist to begin reviewing. */
    public void startReview() {
        if (kycStatus != KycStatus.DOCUMENTS_SUBMITTED) {
            throw new BankException(
                    ErrorCode.VALIDATION_FAILED,
                    "Cannot start review unless documents are submitted"
            );
        }
        kycStatus = KycStatus.UNDER_REVIEW;
        updatedAt = Instant.now();
    }

    /** Called by a support agent or automated system when KYC passes. */
    public void approveKyc() {
        if (kycStatus != KycStatus.UNDER_REVIEW) {
            throw new BankException(
                    ErrorCode.VALIDATION_FAILED,
                    "Cannot approve KYC unless it is under review"
            );
        }
        kycStatus          = KycStatus.VERIFIED;
        kycRejectionReason = null;
        updatedAt          = Instant.now();
    }

    /** Called by a support agent when KYC documents are insufficient. */
    public void rejectKyc(String reason) {
        if (kycStatus != KycStatus.UNDER_REVIEW) {
            throw new BankException(
                    ErrorCode.VALIDATION_FAILED,
                    "Cannot reject KYC unless it is under review"
            );
        }
        kycStatus          = KycStatus.REJECTED;
        kycRejectionReason = reason;
        updatedAt          = Instant.now();
    }

    // ── Domain queries ────────────────────────────────────

    public boolean isKycVerified()  { return kycStatus == KycStatus.VERIFIED; }
    public boolean canOpenAccount() { return isKycVerified(); }
    public String  getFullName()    { return firstName + " " + lastName; }

    // ── Getters ───────────────────────────────────────────

    public String    getId()                 { return id; }
    public String    getAuthUserId()         { return authUserId; }
    public Bvn       getBvn()                { return bvn; }
    public String    getNin()                { return nin; }
    public String    getFirstName()          { return firstName; }
    public String    getLastName()           { return lastName; }
    public String    getMiddleName()         { return middleName; }
    public LocalDate getDateOfBirth()        { return dateOfBirth; }
    public String    getPhoneNumber()        { return phoneNumber; }
    public String    getEmail()              { return email; }
    public String    getAddress()            { return address; }
    public String    getStateOfOrigin()      { return stateOfOrigin; }
    public KycStatus getKycStatus()          { return kycStatus; }
    public String    getKycRejectionReason() { return kycRejectionReason; }
    public List<KycDocument> getDocuments()  {
        return Collections.unmodifiableList(documents);
    }
    public Instant   getCreatedAt()          { return createdAt; }
    public Instant   getUpdatedAt()          { return updatedAt; }

    public void setAddress(String address) {
        this.address   = address;
        this.updatedAt = Instant.now();
    }

    public void setStateOfOrigin(String state) {
        this.stateOfOrigin = state;
        this.updatedAt     = Instant.now();
    }
}