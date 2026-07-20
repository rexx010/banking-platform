package com.bankplatform.account.adapter.out.messaging;

import com.bankplatform.account.domain.model.Account;
import com.bankplatform.account.domain.port.out.AccountEventPublisher;
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
public class KafkaAccountEventPublisher implements AccountEventPublisher {

    private static final String TOPIC = "account.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishAccountCreated(Account account) {
        var event = new AccountCreatedEvent(
                IdGenerator.generate(),
                Instant.now(),
                MDC.get("traceId"),
                account.getId(),
                account.getAccountNumber().getValue(),
                account.getBankCode().getValue(),
                account.getOwnerBvn(),
                account.getAccountType().name(),
                account.getCurrency()
        );
        publish(account.getId(), event, "AccountCreated");
    }

    @Override
    public void publishAccountStatusChanged(
            Account account, String previousStatus, String reason
    ) {
        var event = new AccountStatusChangedEvent(
                IdGenerator.generate(),
                Instant.now(),
                MDC.get("traceId"),
                account.getId(),
                account.getAccountNumber().getValue(),
                previousStatus,
                account.getStatus().name(),
                reason
        );
        publish(account.getId(), event, "AccountStatusChanged");
    }

    private void publish(String key, Object event, String type) {
        kafkaTemplate.send(TOPIC, key, event)
                .thenRun(() ->
                        log.info("Published {} event", type))
                .exceptionally(ex -> {
                    log.error("Failed to publish {}: {}", type, ex.getMessage());
                    return null;
                });
    }
}