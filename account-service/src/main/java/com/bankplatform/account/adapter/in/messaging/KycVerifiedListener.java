package com.bankplatform.account.adapter.in.messaging;

import com.bankplatform.account.application.usecase.AccountApplicationService;
import com.bankplatform.shared.events.DomainEvents.KycVerifiedEvent;
import com.bankplatform.shared.logging.MaskingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KycVerifiedListener {

    private final AccountApplicationService accountService;

    @KafkaListener(
            topics   = "identity.events",
            groupId  = "account-service-kyc",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onKycVerified(
            @Payload KycVerifiedEvent event,
            Acknowledgment acknowledgment
    ) {
        // Only handle VERIFIED events — filter out REJECTED
        if (!"SYSTEM".equals(event.verifiedBy())
                && !"MANUAL".equals(event.verifiedBy())) {
            acknowledgment.acknowledge();
            return;
        }

        log.info("KYC verified event received bvn={} customerId={}",
                MaskingUtil.maskBvn(event.bvn()),
                event.customerId());

        try {
            accountService.activateAccountsForBvn(event.bvn());
            acknowledgment.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to activate accounts for bvn={}: {}",
                    MaskingUtil.maskBvn(event.bvn()), ex.getMessage());
            // Do NOT acknowledge — Kafka will redeliver
            // so we get another chance to process this event
        }
    }
}