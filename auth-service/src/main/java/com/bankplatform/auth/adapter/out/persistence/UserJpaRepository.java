package com.bankplatform.auth.adapter.out.persistence;

import com.bankplatform.auth.adapter.out.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring generates the implementation at startup.
 * You write the method signatures, Spring writes the SQL.
 *
 * This interface is package-private (no public keyword).
 * Only UserPersistenceAdapter in this package can use it.
 * Nothing outside the persistence adapter can bypass the
 * domain and call the database directly.
 */
@Repository
interface UserJpaRepository extends JpaRepository<UserJpaEntity, String> {
    Optional<UserJpaEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}
