package com.bankplatform.identity.adapter.out.persistence;

import com.bankplatform.identity.adapter.out.persistence.entity.CustomerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

interface CustomerJpaRepository
        extends JpaRepository<CustomerJpaEntity, String> {

    Optional<CustomerJpaEntity> findByBvn(String bvn);
    Optional<CustomerJpaEntity> findByAuthUserId(String authUserId);
    Optional<CustomerJpaEntity> findByNin(String nin);
    boolean existsByBvn(String bvn);
    boolean existsByNin(String nin);
}