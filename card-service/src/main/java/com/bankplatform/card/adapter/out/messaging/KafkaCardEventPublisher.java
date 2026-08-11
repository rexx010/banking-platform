package com.bankplatform.card.adapter.out.messaging;

import com.bankplatform.card.domain.model.Card;
import com.bankplatform.card.domain.port.out.CardEventPublisher;
import com.bankplatform.shared.events.DomainEvents.*;
import com.bankplatform.shared.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaCardEventPublisher implements CardEventPublisher {

    private static final String TOPIC = "card.events";
    private final KafkaTemplate<String, Object> kafka;

    @Override
    public void publishCardIssued(Card card) {
        var event = new CardIssuedEvent(
                IdGenerator.generate(), Instant.now(), MDC.get("traceId"),
                card.getId(),
                card.getMaskedNumber(),  // NEVER the full number
                card.getLinkedNuban(),
                String.format("%02d", card.getExpiryDate().getMonthValue()),
                String.valueOf(card.getExpiryDate().getYear()),
                card.getCardNetwork().name()
        );
        publish(card.getId(), event, "CardIssued");
    }

    @Override
    public void publishTransactionApproved(
            Card card, long amountKobo, String merchantName, String eventId
    ) {
        var event = new CardTransactionEvent(
                eventId, Instant.now(), MDC.get("traceId"),
                card.getId(), card.getMaskedNumber(),
                card.getLinkedNuban(), amountKobo,
                merchantName, "APPROVED", null
        );
        publish(card.getId(), event, "CardTransactionApproved");
    }

    @Override
    public void publishTransactionDeclined(
            Card card, long amountKobo, String merchantName,
            String declineReason, String eventId
    ) {
        var event = new CardTransactionEvent(
                eventId, Instant.now(), MDC.get("traceId"),
                card.getId(), card.getMaskedNumber(),
                card.getLinkedNuban(), amountKobo,
                merchantName, "DECLINED", declineReason
        );
        publish(card.getId(), event, "CardTransactionDeclined");
    }

    private void publish(String key, Object event, String type) {
        kafka.send(TOPIC, key, event)
                .thenRun(() -> log.info("Published {}", type))
                .exceptionally(ex -> {
                    log.error("Failed to publish {}: {}", type, ex.getMessage());
                    return null;
                });
    }
}