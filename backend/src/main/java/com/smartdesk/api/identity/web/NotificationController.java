package com.smartdesk.api.identity.web;

import com.smartdesk.api.identity.service.NotificationService;
import com.smartdesk.api.identity.service.NotificationView;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService service;
    public NotificationController(NotificationService service) { this.service = service; }
    @GetMapping public List<NotificationView> list(@AuthenticationPrincipal Jwt jwt) { return service.list(UUID.fromString(jwt.getSubject())); }
    @PatchMapping("/{id}/read") public NotificationView markRead(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) { return service.markRead(id, UUID.fromString(jwt.getSubject())); }
}
