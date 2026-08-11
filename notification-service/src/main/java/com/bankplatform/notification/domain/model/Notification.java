package com.bankplatform.notification.domain.model;

import com.bankplatform.shared.util.IdGenerator;
import java.time.Instant;

/**
 * Notification domain entity.
 *
 * Records every attempted notification for audit purposes.
 * Even failed notifications are recorded — they tell support agents
 * why a customer did not receive an alert.
 */
public class Notification {

    private final String              id;
    private final String              recipientId;       // userId
    private final NotificationChannel channel;
    private final String              eventType;         // "TRANSFER_COMPLETED" etc.
    private final String              message;
    private final String              recipient;         // phone, email, or device token
    private       NotificationStatus  status;
    private       String              failureReason;
    private       int                 attemptCount;
    private final String              traceId;           // links to originating request
    private final Instant             createdAt;
    private       Instant             sentAt;

    // ── Static factory ────────────────────────────────────

    public static Notification create(
            String              recipientId,
            NotificationChannel channel,
            String              eventType,
            String              message,
            String              recipient,
            String              traceId
    ) {
        return new Notification(
                IdGenerator.generate(),
                recipientId, channel, eventType,
                message, recipient,
                NotificationStatus.PENDING,
                null, 0, traceId, Instant.now(), null
        );
    }

    private Notification(
            String id, String recipientId, NotificationChannel channel,
            String eventType, String message, String recipient,
            NotificationStatus status, String failureReason,
            int attemptCount, String traceId,
            Instant createdAt, Instant sentAt
    ) {
        this.id           = id;
        this.recipientId  = recipientId;
        this.channel      = channel;
        this.eventType    = eventType;
        this.message      = message;
        this.recipient    = recipient;
        this.status       = status;
        this.failureReason= failureReason;
        this.attemptCount = attemptCount;
        this.traceId      = traceId;
        this.createdAt    = createdAt;
        this.sentAt       = sentAt;
    }

    // ── State transitions ─────────────────────────────────

    public void markSent() {
        this.status       = NotificationStatus.SENT;
        this.sentAt       = Instant.now();
        this.attemptCount++;
    }

    public void markFailed(String reason) {
        this.status        = NotificationStatus.FAILED;
        this.failureReason = reason;
        this.attemptCount++;
    }

    // ── Getters ───────────────────────────────────────────

    public String              getId()            { return id; }
    public String              getRecipientId()   { return recipientId; }
    public NotificationChannel getChannel()       { return channel; }
    public String              getEventType()     { return eventType; }
    public String              getMessage()       { return message; }
    public String              getRecipient()     { return recipient; }
    public NotificationStatus  getStatus()        { return status; }
    public String              getFailureReason() { return failureReason; }
    public int                 getAttemptCount()  { return attemptCount; }
    public String              getTraceId()       { return traceId; }
    public Instant             getCreatedAt()     { return createdAt; }
    public Instant             getSentAt()        { return sentAt; }
}