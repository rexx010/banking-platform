package com.bankplatform.transfer.domain.port.out;

/**
 * OUT-PORT: verifies a user's transaction PIN.
 *
 * Implemented by AuthServiceClient which calls
 * auth-service's internal PIN verification endpoint.
 *
 * PIN verification happens BEFORE any money movement.
 * If this throws or returns false, the transfer does not proceed.
 */
public interface PinVerificationPort {

    void verifyOrThrow(String userId, String rawPin);
}