package com.smartdesk.api.identity.service;

import com.smartdesk.api.identity.model.Notification;
import com.smartdesk.api.identity.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {
    private final NotificationRepository repository;
    public NotificationService(NotificationRepository repository) { this.repository = repository; }
    @Transactional(readOnly = true)
    public List<NotificationView> list(UUID userId) { return repository.findAllByRecipient_IdOrderByCreatedAtDesc(userId).stream().map(NotificationService::view).toList(); }
    @Transactional
    public NotificationView markRead(UUID id, UUID userId) {
        Notification notification = repository.findByIdAndRecipient_Id(id, userId)
                .orElseThrow(NotificationNotFoundException::new);
        notification.markRead();
        return view(repository.saveAndFlush(notification));
    }
    private static NotificationView view(Notification item) { return new NotificationView(item.getId(), item.getTicket() == null ? null : item.getTicket().getId(), item.getMessage(), item.getReadAt() != null, item.getCreatedAt()); }
}
