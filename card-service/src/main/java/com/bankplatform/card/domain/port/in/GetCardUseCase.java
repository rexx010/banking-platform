package com.bankplatform.card.domain.port.in;

import com.bankplatform.card.domain.model.Card;
import java.util.List;

public interface GetCardUseCase {
    Card getById(String cardId);
    List<Card> getByOwnerUserId(String userId);
    Card getByLinkedNuban(String accountNumber);
}