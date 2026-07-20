package com.bankplatform.identity.adapter.out.messaging;

import com.bankplatform.identity.domain.model.Customer;
import com.bankplatform.identity.domain.port.out.IdentityEventPublisher;
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
public class KafkaIdentityEventPublisher implements IdentityEventPublisher {

    private static final String TOPIC = "identity.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishBvnCreated(Customer customer) {
        var event = new BvnCreatedEvent(
                IdGenerator.generate(),
                Instant.now(),
                MDC.get("traceId"),
                customer.getBvn().getValue(),  // raw value stored in event
                customer.getId(),
                customer.getFullName()
        );
        publish(customer.getId(), event, "BvnCreated");
    }

    @Override
    public void publishKycVerified(Customer customer) {
        var event = new KycVerifiedEvent(
                IdGenerator.generate(),
                Instant.now(),
                MDC.get("traceId"),
                customer.getBvn().getValue(),
                customer.getId(),
                "SYSTEM"
        );
        publish(customer.getId(), event, "KycVerified");
    }

    @Override
    public void publishKycRejected(Customer customer) {
        // Reuse KycVerifiedEvent with REJECTED marker
        // In a mature system you'd create a KycRejectedEvent
        var event = new KycVerifiedEvent(
                IdGenerator.generate(),
                Instant.now(),
                MDC.get("traceId"),
                customer.getBvn().getValue(),
                customer.getId(),
                "REJECTED"
        );
        publish(customer.getId(), event, "KycRejected");
    }

    private void publish(String key, Object event, String type) {
        kafkaTemplate.send(TOPIC, key, event)
                .thenRun(() ->
                        log.info("Published {} event customerId={}", type, key))
                .exceptionally(ex -> {
                    log.error("Failed to publish {} event customerId={}: {}",
                            type, key, ex.getMessage());
                    return null;
                });
    }
}