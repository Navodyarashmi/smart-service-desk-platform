package com.smartdesk.api.identity.repository;

import com.smartdesk.api.identity.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findAllByRecipient_IdOrderByCreatedAtDesc(UUID recipientId);
    Optional<Notification> findByIdAndRecipient_Id(UUID id, UUID recipientId);
}
