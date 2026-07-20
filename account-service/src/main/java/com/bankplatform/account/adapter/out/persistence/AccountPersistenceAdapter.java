package com.bankplatform.account.adapter.out.persistence;

import com.bankplatform.account.adapter.out.persistence.mapper.AccountPersistenceMapper;
import com.bankplatform.account.domain.model.Account;
import com.bankplatform.account.domain.model.AccountNumber;
import com.bankplatform.account.domain.port.out.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AccountPersistenceAdapter implements AccountRepository {

    private final AccountJpaRepository   jpaRepository;
    private final AccountPersistenceMapper mapper;

    @Override
    public Account save(Account account) {
        return mapper.toDomain(
                jpaRepository.save(mapper.toJpaEntity(account))
        );
    }

    @Override
    public Optional<Account> findById(String id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findByAccountNumber(AccountNumber number) {
        return jpaRepository
                .findByAccountNumber(number.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public List<Account> findByOwnerBvn(String bvn) {
        return jpaRepository.findByOwnerBvn(bvn)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByAccountNumber(AccountNumber number) {
        return jpaRepository.existsByAccountNumber(number.getValue());
    }
}