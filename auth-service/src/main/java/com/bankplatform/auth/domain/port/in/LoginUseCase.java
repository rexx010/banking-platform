package com.bankplatform.auth.domain.port.in;

import com.bankplatform.auth.application.usecase.AuthCommands.TokenPair;
import com.bankplatform.auth.application.usecase.AuthCommands.LoginCommand;

public interface LoginUseCase {
    TokenPair login(LoginCommand command);
}
