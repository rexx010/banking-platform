package com.bankplatform.auth.domain.port.in;

import com.bankplatform.auth.application.usecase.AuthCommands.TokenPair;

public interface RefreshTokenUseCase {
    TokenPair refresh(String refreshToken);
}
