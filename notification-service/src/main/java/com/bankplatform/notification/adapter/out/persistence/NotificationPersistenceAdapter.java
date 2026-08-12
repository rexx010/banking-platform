package com.bankplatform.notification.adapter.out.persistence;

import com.bankplatform.notification.adapter.out.persistence.mapper.NotificationPersistenceMapper;
import com.bankplatform.notification.domain.model.Notification;
import com.bankplatform.notification.domain.port.out.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationPersistenceAdapter
        implements NotificationRepository {

    private final NotificationJpaRepository  jpaRepository;
    private final NotificationPersistenceMapper mapper;

    @Override
    public Notification save(Notification notification) {
        return mapper.toDomain(
                jpaRepository.save(mapper.toJpaEntity(notification)));
    }

    @Override
    public List<Notification> findByRecipientId(String recipientId) {
        return jpaRepository
                .findByRecipientIdOrderByCreatedAtDesc(recipientId)
                .stream().map(mapper::toDomain).toList();
    }
}