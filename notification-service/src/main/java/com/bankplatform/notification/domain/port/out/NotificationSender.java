package com.bankplatform.notification.domain.port.out;

import com.bankplatform.notification.domain.model.Notification;

/**
 * OUT-PORT: dispatches notifications via external providers.
 *
 * Implementations:
 *   SMS:   Africa's Talking or Termii API
 *   EMAIL: SendGrid or AWS SES API
 *   PUSH:  Firebase FCM API
 *
 * In dev mode, a console-only implementation logs to stdout
 * instead of calling real APIs — so you can test without
 * paying for actual SMS sends.
 */
public interface NotificationSender {
    void send(Notification notification);
}