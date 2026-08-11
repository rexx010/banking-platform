package com.bankplatform.transfer.adapter.out.persistence;

import com.bankplatform.transfer.adapter.out.persistence.entity.TransferJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface TransferJpaRepository
        extends JpaRepository<TransferJpaEntity, String> {

    Optional<TransferJpaEntity> findByIdempotencyKey(String key);
    List<TransferJpaEntity> findBySourceAccountNumber(String accountNumber);
}