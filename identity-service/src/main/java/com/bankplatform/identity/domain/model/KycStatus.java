package com.bankplatform.identity.domain.model;

/**
 * KYC (Know Your Customer) verification lifecycle states.
 *
 * State machine transitions:
 *
 * PENDING
 *   ↓ (customer uploads documents)
 * DOCUMENTS_SUBMITTED
 *   ↓ (system auto-submits when minimum docs are uploaded)
 * UNDER_REVIEW
 *   ↓                    ↓
 * VERIFIED           REJECTED
 *                        ↓ (customer re-uploads better documents)
 *                    DOCUMENTS_SUBMITTED
 *
 * A VERIFIED customer can open bank accounts.
 * A REJECTED customer must resubmit improved documents.
 * Transitions are enforced by Customer.java domain methods.
 */
public enum KycStatus {

    /** BVN created but no documents uploaded yet. */
    PENDING,

    DOCUMENTS_SUBMITTED,

    UNDER_REVIEW,

    VERIFIED,

    REJECTED
}