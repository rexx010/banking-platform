package com.bankplatform.account.domain.port.out;

import com.bankplatform.account.domain.model.Account;

public interface AccountEventPublisher {
    void publishAccountCreated(Account account);
    void publishAccountStatusChanged(Account account, String previousStatus, String reason);
}