package com.bankplatform.notification.domain.model;

/**
 * The channel through which a notification is delivered.
 *
 * SMS:   Africa's Talking, Termii, or similar provider
 *        Character limit: 160 per segment
 *
 * EMAIL: SendGrid, AWS SES, or similar provider
 *        Supports HTML, attachments, rich formatting
 *
 * PUSH:  Firebase Cloud Messaging (FCM)
 *        Requires device token — only works if app is installed
 *        and notifications are enabled
 *
 * In a real system, a user's preferences determine which
 * channels are active for which event types.
 */
public enum NotificationChannel {
    SMS,
    EMAIL,
    PUSH
}