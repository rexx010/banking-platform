package com.bankplatform.ledger.adapter.out.persistence;

import com.bankplatform.ledger.adapter.out.persistence.mapper.LedgerPersistenceMapper;
import com.bankplatform.ledger.domain.model.LedgerEntry;
import com.bankplatform.ledger.domain.port.out.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LedgerPersistenceAdapter implements LedgerEntryRepository {

    private final LedgerJpaRepository   jpaRepository;
    private final LedgerPersistenceMapper mapper;

    @Override
    public LedgerEntry save(LedgerEntry entry) {
        return mapper.toDomain(
                jpaRepository.save(mapper.toJpaEntity(entry))
        );
    }

    @Override
    public List<LedgerEntry> saveAll(List<LedgerEntry> entries) {
        var entities = entries.stream()
                .map(mapper::toJpaEntity)
                .toList();
        return jpaRepository.saveAll(entities)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByReference(String reference) {
        return jpaRepository.existsByTransactionReference(reference);
    }

    @Override
    public Page<LedgerEntry> findByAccountNumber(
            String accountNumber, Instant from, Instant to, Pageable pageable
    ) {
        return jpaRepository
                .findByAccountNumberAndCreatedAtBetween(
                        accountNumber, from, to, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public long sumCreditsByAccountNumber(String accountNumber) {
        return jpaRepository.sumCreditsByAccountNumber(accountNumber);
    }

    @Override
    public long sumDebitsByAccountNumber(String accountNumber) {
        return jpaRepository.sumDebitsByAccountNumber(accountNumber);
    }
}