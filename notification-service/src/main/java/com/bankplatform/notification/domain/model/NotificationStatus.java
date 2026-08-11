package com.bankplatform.notification.domain.model;

/**
 * Delivery status of a notification.
 *
 * PENDING:    queued, not yet sent
 * SENT:       accepted by the provider (not necessarily delivered)
 * DELIVERED:  confirmed delivered (SMS delivery receipts, email opens)
 * FAILED:     permanent failure — invalid number, bounced email
 */
public enum NotificationStatus {
    PENDING,
    SENT,
    DELIVERED,
    FAILED
}