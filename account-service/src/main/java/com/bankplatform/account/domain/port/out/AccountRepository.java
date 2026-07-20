package com.bankplatform.account.domain.port.out;

import com.bankplatform.account.domain.model.Account;
import com.bankplatform.account.domain.model.AccountNumber;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    Account            save(Account account);
    Optional<Account>  findById(String id);
    Optional<Account>  findByAccountNumber(AccountNumber accountNumber);
    List<Account>      findByOwnerBvn(String bvn);
    boolean            existsByAccountNumber(AccountNumber accountNumber);
}