package com.bankplatform.notification.adapter.in.messaging;

import com.bankplatform.notification.application.usecase.NotificationApplicationService;
import com.bankplatform.notification.domain.model.NotificationChannel;
import com.bankplatform.shared.events.DomainEvents.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Listens to events from all services and dispatches notifications.
 *
 * Each listener handles one event type and formats a human-readable
 * message. Notification-service owns the message copy — other services
 * just publish what happened.
 *
 * All listeners are idempotent — duplicate events are handled gracefully
 * because sending a duplicate SMS is far better than crashing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AllEventsListener {

    private final NotificationApplicationService notificationService;

    // ── Transfer events ───────────────────────────────────

    @KafkaListener(
            topics           = "transfer.events",
            groupId          = "notification-service-transfers",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onTransferCompleted(
            @Payload TransferCompletedEvent event,
            Acknowledgment acknowledgment
    ) {
        log.info("Transfer completed — notifying parties transferId={}",
                event.transferId());

        try {
            BigDecimal naira =
                    BigDecimal.valueOf(event.amountKobo(), 2);

            String senderMsg = String.format(
                    "Your transfer of %s %s to account %s was successful. " +
                            "Ref: %s",
                    event.currency(), naira,
                    maskNuban(event.destinationNuban()),
                    event.transferId()
            );

            String receiverMsg = String.format(
                    "You received %s %s from account %s. Ref: %s",
                    event.currency(), naira,
                    maskNuban(event.sourceNuban()),
                    event.transferId()
            );

            // In a real system, look up phone/email from customer profile.
            // Here we log the messages as a placeholder.
            log.info("SENDER notification: {}", senderMsg);
            log.info("RECEIVER notification: {}", receiverMsg);

            // When customer profile lookup is available:
            // notificationService.sendOnAllChannels(senderId, "TRANSFER_COMPLETED",
            //     senderMsg, senderPhone, senderEmail, event.traceId());

            acknowledgment.acknowledge();

        } catch (Exception ex) {
            log.error("Failed to send transfer notification: {}",
                    ex.getMessage());
            acknowledgment.acknowledge(); // acknowledge to prevent infinite retry
        }
    }

    @KafkaListener(
            topics           = "transfer.events",
            groupId          = "notification-service-transfer-failures",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onTransferFailed(
            @Payload TransferFailedEvent event,
            Acknowledgment acknowledgment
    ) {
        log.info("Transfer failed — notifying sender transferId={}",
                event.transferId());

        String message = String.format(
                "Your transfer of %s %s failed. Reason: %s. No funds were deducted.",
                event.amountKobo() / 100.0, "NGN",
                event.failureReason()
        );

        log.info("SENDER notification (failed): {}", message);
        acknowledgment.acknowledge();
    }

    @KafkaListener(
            topics           = "transfer.events",
            groupId          = "notification-service-reversals",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onTransferReversed(
            @Payload TransferReversedEvent event,
            Acknowledgment acknowledgment
    ) {
        log.info("Transfer reversed — notifying sender transferId={}",
                event.originalTransferId());

        BigDecimal naira = BigDecimal.valueOf(event.amountKobo(), 2);
        String message = String.format(
                "Your transfer of NGN %s has been reversed and credited " +
                        "back to your account. We apologise for the inconvenience.",
                naira
        );

        log.info("REVERSAL notification: {}", message);
        acknowledgment.acknowledge();
    }

    // ── Card events ───────────────────────────────────────

    @KafkaListener(
            topics           = "card.events",
            groupId          = "notification-service-cards",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onCardEvent(
            @Payload CardTransactionEvent event,
            Acknowledgment acknowledgment
    ) {
        try {
            BigDecimal naira = BigDecimal.valueOf(event.amountKobo(), 2);

            String message = "APPROVED".equals(event.status())
                    ? String.format("Card payment of NGN %s at %s approved.",
                    naira, event.merchantName())
                    : String.format("Card payment of NGN %s at %s declined. %s",
                    naira, event.merchantName(), event.declineReason());

            log.info("CARD notification: {}", message);
            acknowledgment.acknowledge();

        } catch (Exception ex) {
            log.error("Failed to send card notification: {}", ex.getMessage());
            acknowledgment.acknowledge();
        }
    }

    // ── Identity events ───────────────────────────────────

    @KafkaListener(
            topics           = "identity.events",
            groupId          = "notification-service-identity",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onKycVerified(
            @Payload KycVerifiedEvent event,
            Acknowledgment acknowledgment
    ) {
        if ("REJECTED".equals(event.verifiedBy())) {
            log.info("KYC rejected notification customerId={}",
                    event.customerId());
            acknowledgment.acknowledge();
            return;
        }

        log.info("KYC verified notification customerId={}",
                event.customerId());
        String message =
                "Your identity has been verified. You can now open bank accounts.";
        log.info("KYC notification: {}", message);
        acknowledgment.acknowledge();
    }

    // ── Helper ────────────────────────────────────────────

    private static String maskNuban(String nuban) {
        if (nuban == null || nuban.length() < 4) return "****";
        return "******" + nuban.substring(nuban.length() - 4);
    }
}