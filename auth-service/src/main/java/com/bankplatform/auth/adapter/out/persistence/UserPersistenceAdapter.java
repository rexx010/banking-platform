package com.bankplatform.auth.adapter.out.persistence;

import com.bankplatform.auth.adapter.out.persistence.mapper.UserPersistenceMapper;
import com.bankplatform.auth.domain.model.User;
import com.bankplatform.auth.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implements the domain's UserRepository using Spring Data JPA.
 *
 * The domain calls UserRepository (an interface it defines).
 * Spring injects this class wherever UserRepository is needed.
 * The domain never imports anything from this package.
 *
 * If you switched to MongoDB you would write a new adapter
 * MongoUserAdapter implements UserRepository.
 * The domain, use cases, and controller would not change.
 */
@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepository {
    private final UserJpaRepository jpaRepository;
    private final UserPersistenceMapper mapper;

    @Override
    public User save(User user) {
        var entity = mapper.toJpaEntity(user);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(String id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
}
