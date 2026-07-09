package com.bankplatform.auth.domain.port.in;

import com.bankplatform.auth.application.usecase.AuthCommands.RegisterUserCommand;
import com.bankplatform.auth.domain.model.User;

public interface RegisterUserUseCase {
    User register(RegisterUserCommand command);
}
