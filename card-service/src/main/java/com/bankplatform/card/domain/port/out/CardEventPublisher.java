package com.bankplatform.card.domain.port.out;

import com.bankplatform.card.domain.model.Card;

public interface CardEventPublisher {
    void publishCardIssued(Card card);
    void publishTransactionApproved(Card card, long amountKobo,
                                    String merchantName, String eventId);
    void publishTransactionDeclined(Card card, long amountKobo,
                                    String merchantName, String declineReason, String eventId);
}