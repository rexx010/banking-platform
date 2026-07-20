package com.bankplatform.account.domain.port.in;

import com.bankplatform.account.domain.model.Account;
import java.util.List;

public interface GetAccountUseCase {
    Account getByAccountNumber(String accountNumber);
    Account getById(String accountId);
    List<Account> getByBvn(String bvn);
}