package com.bankplatform.notification.adapter.out.sender;

import com.bankplatform.notification.domain.model.Notification;
import com.bankplatform.notification.domain.port.out.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!prod")
public class ConsoleNotificationSender implements NotificationSender {

    @Override
    public void send(Notification notification) {
        log.info("""
            ┌─────────────────────────────────────
            │ NOTIFICATION [{}]
            │ Channel:    {}
            │ Event:      {}
            │ Recipient:  {}
            │ Message:    {}
            │ TraceId:    {}
            └─────────────────────────────────────
            """,
                notification.getId(),
                notification.getChannel(),
                notification.getEventType(),
                notification.getRecipient(),
                notification.getMessage(),
                notification.getTraceId()
        );
    }
}