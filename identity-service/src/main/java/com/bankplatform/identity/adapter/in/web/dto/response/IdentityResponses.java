package com.bankplatform.identity.adapter.in.web.dto.response;

import java.time.Instant;
import java.time.LocalDate;

public final class IdentityResponses {

    private IdentityResponses() {}

    /** Full customer profile — returned after BVN creation and on profile fetch. */
    public record CustomerResponse(
            String    customerId,
            String    bvn,              // ALWAYS masked: "222*****678"
            String    fullName,
            String    firstName,
            String    lastName,
            String    phoneNumber,
            String    email,
            LocalDate dateOfBirth,
            String    kycStatus,
            String    kycRejectionReason,
            int       documentCount,
            Instant   createdAt
    ) {}

    /**
     * Lightweight response for internal BVN verification.
     * Called by account-service before opening an account.
     * Returns only what account-service needs to know.
     */
    public record BvnVerificationResponse(
            String  bvn,           // masked
            String  customerId,
            String  fullName,
            boolean kycVerified,
            String  phoneNumber,
            String  email
    ) {}
}