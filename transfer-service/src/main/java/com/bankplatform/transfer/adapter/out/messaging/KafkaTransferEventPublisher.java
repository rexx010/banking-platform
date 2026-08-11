package com.bankplatform.transfer.adapter.out.messaging;

import com.bankplatform.shared.events.DomainEvents.*;
import com.bankplatform.shared.util.IdGenerator;
import com.bankplatform.transfer.domain.model.Transfer;
import com.bankplatform.transfer.domain.port.out.TransferEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaTransferEventPublisher implements TransferEventPublisher {

    private static final String TOPIC = "transfer.events";
    private final KafkaTemplate<String, Object> kafka;

    @Override
    public void publishInitiated(Transfer t) {
        publish(t.getId(), new TransferInitiatedEvent(
                IdGenerator.generate(), Instant.now(), MDC.get("traceId"),
                t.getId(), t.getIdempotencyKey(),
                t.getSourceAccountNumber(), t.getDestinationAccountNumber(),
                t.getDestinationBankCode(), t.getAmountKobo(),
                t.getCurrency(), t.getNarration()
        ), "TransferInitiated");
    }

    @Override
    public void publishCompleted(Transfer t) {
        publish(t.getId(), new TransferCompletedEvent(
                IdGenerator.generate(), Instant.now(), MDC.get("traceId"),
                t.getId(), t.getSourceAccountNumber(),
                t.getDestinationAccountNumber(),
                t.getAmountKobo(), t.getCurrency(), t.getNarration()
        ), "TransferCompleted");
    }

    @Override
    public void publishFailed(Transfer t) {
        publish(t.getId(), new TransferFailedEvent(
                IdGenerator.generate(), Instant.now(), MDC.get("traceId"),
                t.getId(), t.getSourceAccountNumber(),
                t.getDestinationAccountNumber(), t.getAmountKobo(),
                t.getFailureReason(), "TRANSFER_FAILED"
        ), "TransferFailed");
    }

    @Override
    public void publishReversed(Transfer t) {
        publish(t.getId(), new TransferReversedEvent(
                IdGenerator.generate(), Instant.now(), MDC.get("traceId"),
                t.getId(), t.getSourceAccountNumber(),
                t.getAmountKobo(), t.getFailureReason()
        ), "TransferReversed");
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