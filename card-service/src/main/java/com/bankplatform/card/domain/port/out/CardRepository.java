package com.bankplatform.card.domain.port.out;

import com.bankplatform.card.domain.model.Card;
import java.util.List;
import java.util.Optional;

public interface CardRepository {
    Card           save(Card card);
    Optional<Card> findById(String id);
    Optional<Card> findByCardNumber(String cardNumber);
    Optional<Card> findByLinkedNuban(String nuban);
    List<Card>     findByOwnerUserId(String userId);
    boolean        existsByCardNumber(String cardNumber);
    boolean        existsByLinkedNuban(String nuban);
}