package com.bankplatform.notification.application.usecase;

import com.bankplatform.notification.domain.model.Notification;
import com.bankplatform.notification.domain.model.NotificationChannel;
import com.bankplatform.notification.domain.port.out.NotificationRepository;
import com.bankplatform.notification.domain.port.out.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Orchestrates notification creation, dispatch, and recording.
 *
 * Parallel dispatch: SMS, email, and push are sent concurrently
 * using CompletableFuture so total send time = slowest channel,
 * not sum of all channels.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationApplicationService {

    private final NotificationRepository repository;
    private final NotificationSender     sender;

    public CompletableFuture<Notification> send(
            String              recipientId,
            NotificationChannel channel,
            String              eventType,
            String              message,
            String              recipient,
            String              traceId
    ) {
        Notification notification = Notification.create(
                recipientId, channel, eventType,
                message, recipient, traceId
        );

        Notification saved = repository.save(notification);

        // Execute the send on a separate thread — non-blocking
        return CompletableFuture.supplyAsync(() -> {
            try {
                sender.send(saved);
                saved.markSent();
                log.info("Notification sent channel={} recipientId={} event={}",
                        channel, recipientId, eventType);
            } catch (Exception ex) {
                saved.markFailed(ex.getMessage());
                log.error("Notification failed channel={} recipientId={}: {}",
                        channel, recipientId, ex.getMessage());
            }
            return repository.save(saved);
        });
    }

    /**
     * Sends notifications on multiple channels in parallel.
     * Returns when all channels have completed (success or failure).
     */
    public void sendOnAllChannels(
            String   recipientId,
            String   eventType,
            String   message,
            String   phoneNumber,
            String   email,
            String   traceId
    ) {
        CompletableFuture<Notification> smsFuture = null;
        CompletableFuture<Notification> emailFuture = null;

        if (phoneNumber != null && !phoneNumber.isBlank()) {
            smsFuture = send(recipientId, NotificationChannel.SMS,
                    eventType, message, phoneNumber, traceId);
        }
        if (email != null && !email.isBlank()) {
            emailFuture = send(recipientId, NotificationChannel.EMAIL,
                    eventType, message, email, traceId);
        }

        // Wait for all to complete
        if (smsFuture != null && emailFuture != null) {
            CompletableFuture.allOf(smsFuture, emailFuture).join();
        } else if (smsFuture != null) {
            smsFuture.join();
        } else if (emailFuture != null) {
            emailFuture.join();
        }
    }
}