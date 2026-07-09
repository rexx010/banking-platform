package com.bankplatform.auth.domain.port.in;

public interface SetTransactionPinUseCase {
    void setPin(String userId, String rawPin, String currentPassword);
}
