package com.bankplatform.notification.adapter.out.persistence;

import com.bankplatform.notification.adapter.out.persistence.entity.NotificationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface NotificationJpaRepository
        extends JpaRepository<NotificationJpaEntity, String> {

    List<NotificationJpaEntity> findByRecipientIdOrderByCreatedAtDesc(
            String recipientId);
}