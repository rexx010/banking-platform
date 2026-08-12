package com.bankplatform.notification.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name    = "notifications",
        indexes = {
                @Index(name = "idx_notif_recipient",
                        columnList = "recipient_id,created_at"),
                @Index(name = "idx_notif_status",
                        columnList = "status"),
                @Index(name = "idx_notif_trace",
                        columnList = "trace_id")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationJpaEntity {

    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "recipient_id", nullable = false, length = 36)
    private String recipientId;

    @Column(name = "channel", nullable = false, length = 20)
    private String channel;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * The actual destination:
     * SMS   → phone number e.g. "+2348012345678"
     * EMAIL → email address e.g. "ade@example.com"
     * PUSH  → FCM device token
     */
    @Column(name = "recipient", nullable = false, length = 255)
    private String recipient;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "trace_id", length = 100)
    private String traceId;

    @CreationTimestamp
    @Column(name = "created_at",
            nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;
}