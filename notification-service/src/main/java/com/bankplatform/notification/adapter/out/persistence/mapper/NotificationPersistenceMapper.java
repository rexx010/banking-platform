package com.bankplatform.notification.adapter.out.persistence.mapper;

import com.bankplatform.notification.adapter.out.persistence.entity.NotificationJpaEntity;
import com.bankplatform.notification.domain.model.Notification;
import com.bankplatform.notification.domain.model.NotificationChannel;
import com.bankplatform.notification.domain.model.NotificationStatus;
import org.mapstruct.*;

@Mapper(
        componentModel       = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface NotificationPersistenceMapper {

    // Domain → JPA
    @Mapping(source = "channel",   target = "channel",
            qualifiedByName = "channelToString")
    @Mapping(source = "status",    target = "status",
            qualifiedByName = "statusToString")
    NotificationJpaEntity toJpaEntity(Notification notification);

    // JPA → Domain (manual — private constructor)
    default Notification toDomain(NotificationJpaEntity e) {
        if (e == null) return null;
        try {
            var ctor = Notification.class.getDeclaredConstructor(
                    String.class, String.class,
                    NotificationChannel.class, String.class,
                    String.class, String.class,
                    NotificationStatus.class, String.class,
                    int.class, String.class,
                    java.time.Instant.class, java.time.Instant.class
            );
            ctor.setAccessible(true);
            return ctor.newInstance(
                    e.getId(), e.getRecipientId(),
                    NotificationChannel.valueOf(e.getChannel()),
                    e.getEventType(), e.getMessage(),
                    e.getRecipient(),
                    NotificationStatus.valueOf(e.getStatus()),
                    e.getFailureReason(), e.getAttemptCount(),
                    e.getTraceId(), e.getCreatedAt(), e.getSentAt()
            );
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to reconstruct Notification: "
                            + ex.getMessage(), ex);
        }
    }

    @Named("channelToString")
    default String channelToString(NotificationChannel c) {
        return c == null ? null : c.name();
    }

    @Named("statusToString")
    default String statusToString(NotificationStatus s) {
        return s == null ? null : s.name();
    }
}