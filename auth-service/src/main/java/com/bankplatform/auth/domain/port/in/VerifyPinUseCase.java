package com.bankplatform.auth.domain.port.in;

/**
 * IN-PORT: verifies a transaction PIN for a given user.
 * Called internally by transfer-service and card-service
 * before authorising money movements.
 */
public interface VerifyPinUseCase {
    boolean verifyPin(String userId, String rawPin);
}