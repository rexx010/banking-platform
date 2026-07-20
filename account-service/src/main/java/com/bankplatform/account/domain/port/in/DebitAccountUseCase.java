package com.bankplatform.account.domain.port.in;

import com.bankplatform.account.application.usecase.AccountCommands.DebitCommand;
import com.bankplatform.account.domain.model.Account;

public interface DebitAccountUseCase {
    Account debit(DebitCommand command);
}