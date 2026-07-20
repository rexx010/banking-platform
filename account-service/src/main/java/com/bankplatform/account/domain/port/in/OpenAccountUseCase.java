package com.bankplatform.account.domain.port.in;

import com.bankplatform.account.application.usecase.AccountCommands.OpenAccountCommand;
import com.bankplatform.account.domain.model.Account;

public interface OpenAccountUseCase {
    Account openAccount(OpenAccountCommand command);
}