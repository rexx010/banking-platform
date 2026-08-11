package com.bankplatform.card.adapter.out.persistence;

import com.bankplatform.card.adapter.out.persistence.mapper.CardPersistenceMapper;
import com.bankplatform.card.domain.model.Card;
import com.bankplatform.card.domain.port.out.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CardPersistenceAdapter implements CardRepository {

    private final CardJpaRepository   jpaRepository;
    private final CardPersistenceMapper mapper;

    @Override
    public Card save(Card card) {
        return mapper.toDomain(
                jpaRepository.save(mapper.toJpaEntity(card)));
    }

    @Override
    public Optional<Card> findById(String id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Card> findByCardNumber(String cardNumber) {
        return jpaRepository.findByCardNumber(cardNumber)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Card> findByLinkedNuban(String nuban) {
        return jpaRepository.findByLinkedNuban(nuban)
                .map(mapper::toDomain);
    }

    @Override
    public List<Card> findByOwnerUserId(String userId) {
        return jpaRepository.findByOwnerUserId(userId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByCardNumber(String cardNumber) {
        return jpaRepository.existsByCardNumber(cardNumber);
    }

    @Override
    public boolean existsByLinkedNuban(String nuban) {
        return jpaRepository.existsByLinkedNuban(nuban);
    }
}