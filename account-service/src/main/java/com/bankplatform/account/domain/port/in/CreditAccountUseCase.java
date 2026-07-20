package com.bankplatform.account.domain.port.in;

import com.bankplatform.account.application.usecase.AccountCommands.CreditCommand;
import com.bankplatform.account.domain.model.Account;

public interface CreditAccountUseCase {
    Account credit(CreditCommand command);
}