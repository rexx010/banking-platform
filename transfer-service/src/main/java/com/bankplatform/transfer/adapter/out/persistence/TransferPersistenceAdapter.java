package com.bankplatform.transfer.adapter.out.persistence;

import com.bankplatform.transfer.adapter.out.persistence.mapper.TransferPersistenceMapper;
import com.bankplatform.transfer.domain.model.Transfer;
import com.bankplatform.transfer.domain.port.out.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TransferPersistenceAdapter implements TransferRepository {

    private final TransferJpaRepository    jpaRepository;
    private final TransferPersistenceMapper mapper;

    @Override
    public Transfer save(Transfer transfer) {
        return mapper.toDomain(
                jpaRepository.save(mapper.toJpaEntity(transfer)));
    }

    @Override
    public Optional<Transfer> findById(String id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Transfer> findByIdempotencyKey(String key) {
        return jpaRepository.findByIdempotencyKey(key)
                .map(mapper::toDomain);
    }

    @Override
    public List<Transfer> findBySourceAccountNumber(String accountNumber) {
        return jpaRepository.findBySourceAccountNumber(accountNumber)
                .stream().map(mapper::toDomain).toList();
    }
}