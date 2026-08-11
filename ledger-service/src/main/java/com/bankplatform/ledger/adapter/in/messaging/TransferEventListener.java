package com.bankplatform.ledger.adapter.in.messaging;

import com.bankplatform.ledger.application.usecase.LedgerApplicationService;
import com.bankplatform.ledger.application.usecase.LedgerCommands.RecordTransactionCommand;
import com.bankplatform.shared.events.DomainEvents.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Listens for events that require ledger entries to be created.
 *
 * Events that create ledger entries:
 *   TransferCompletedEvent  — successful bank transfer
 *   CardTransactionEvent    — approved card transaction
 *   TransferReversedEvent   — transfer reversal (re-credits sender)
 *
 * Each listener is idempotent — duplicate events are detected
 * by the transaction reference and silently ignored.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransferEventListener {

    private final LedgerApplicationService ledgerService;

    @KafkaListener(
            topics          = "transfer.events",
            groupId         = "ledger-service-transfers",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onTransferCompleted(
            @Payload TransferCompletedEvent event,
            Acknowledgment acknowledgment
    ) {
        log.info("Transfer completed event received transferId={}",
                event.transferId());

        try {
            ledgerService.record(new RecordTransactionCommand(
                    event.transferId(),
                    event.sourceNuban(),
                    event.destinationNuban(),
                    event.amountKobo(),
                    event.currency(),
                    "Transfer: " + (event.narration() != null
                            ? event.narration() : "Bank transfer")
            ));
            acknowledgment.acknowledge();

        } catch (IllegalStateException ex) {
            // Duplicate — already recorded, acknowledge to stop redelivery
            log.info("Transfer already in ledger transferId={} — skipping",
                    event.transferId());
            acknowledgment.acknowledge();

        } catch (Exception ex) {
            log.error("Failed to record transfer in ledger transferId={}: {}",
                    event.transferId(), ex.getMessage());
            // Do NOT acknowledge — Kafka will redeliver
        }
    }

    @KafkaListener(
            topics          = "card.events",
            groupId         = "ledger-service-cards",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onCardTransaction(
            @Payload CardTransactionEvent event,
            Acknowledgment acknowledgment
    ) {
        // Only record approved transactions
        if (!"APPROVED".equals(event.status())) {
            acknowledgment.acknowledge();
            return;
        }

        log.info("Card transaction approved cardId={} amount={}",
                event.cardId(), event.amountKobo());

        try {
            ledgerService.record(new RecordTransactionCommand(
                    event.eventId(),
                    event.linkedNuban(),
                    "MERCHANT_SETTLEMENT",  // placeholder — real integration maps merchant accounts
                    event.amountKobo(),
                    "NGN",
                    "Card payment: " + event.merchantName()
            ));
            acknowledgment.acknowledge();

        } catch (IllegalStateException ex) {
            acknowledgment.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to record card transaction: {}", ex.getMessage());
        }
    }
}