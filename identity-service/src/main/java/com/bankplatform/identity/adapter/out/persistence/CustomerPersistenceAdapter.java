package com.bankplatform.identity.adapter.out.persistence;

import com.bankplatform.identity.adapter.out.persistence.entity.KycDocumentJpaEntity;
import com.bankplatform.identity.adapter.out.persistence.mapper.CustomerPersistenceMapper;
import com.bankplatform.identity.domain.model.Customer;
import com.bankplatform.identity.domain.port.out.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomerPersistenceAdapter implements CustomerRepository {

    private final CustomerJpaRepository   jpaRepository;
    private final CustomerPersistenceMapper mapper;

    @Override
    public Customer save(Customer customer) {
        var entity = mapper.toJpaEntity(customer);

        // Manually sync documents since mapper ignores them
        entity.getDocuments().clear();
        for (var doc : customer.getDocuments()) {
            var docEntity = mapper.toDocumentEntity(doc);
            docEntity.setCustomer(entity);
            entity.getDocuments().add(docEntity);
        }

        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Customer> findById(String id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Customer> findByBvn(String bvn) {
        return jpaRepository.findByBvn(bvn).map(mapper::toDomain);
    }

    @Override
    public Optional<Customer> findByAuthUserId(String authUserId) {
        return jpaRepository.findByAuthUserId(authUserId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Customer> findByNin(String nin) {
        return jpaRepository.findByNin(nin).map(mapper::toDomain);
    }

    @Override
    public boolean existsByBvn(String bvn) {
        return jpaRepository.existsByBvn(bvn);
    }

    @Override
    public boolean existsByNin(String nin) {
        return jpaRepository.existsByNin(nin);
    }
}