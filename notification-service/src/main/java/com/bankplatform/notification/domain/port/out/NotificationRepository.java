package com.bankplatform.notification.domain.port.out;

import com.bankplatform.notification.domain.model.Notification;
import java.util.List;

public interface NotificationRepository {
    Notification       save(Notification notification);
    List<Notification> findByRecipientId(String recipientId);
}