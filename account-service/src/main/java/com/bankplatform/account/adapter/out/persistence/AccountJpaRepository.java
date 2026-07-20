package com.bankplatform.account.adapter.out.persistence;

import com.bankplatform.account.adapter.out.persistence.entity.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface AccountJpaRepository
        extends JpaRepository<AccountJpaEntity, String> {

    Optional<AccountJpaEntity> findByAccountNumber(String accountNumber);
    List<AccountJpaEntity>     findByOwnerBvn(String ownerBvn);
    boolean                    existsByAccountNumber(String accountNumber);
}