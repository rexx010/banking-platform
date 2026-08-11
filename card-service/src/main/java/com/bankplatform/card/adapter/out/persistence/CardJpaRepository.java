package com.bankplatform.card.adapter.out.persistence;

import com.bankplatform.card.adapter.out.persistence.entity.CardJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface CardJpaRepository
        extends JpaRepository<CardJpaEntity, String> {

    Optional<CardJpaEntity> findByCardNumber(String cardNumber);
    Optional<CardJpaEntity> findByLinkedNuban(String nuban);
    List<CardJpaEntity>     findByOwnerUserId(String userId);
    boolean                 existsByCardNumber(String cardNumber);
    boolean                 existsByLinkedNuban(String nuban);
}