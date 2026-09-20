package com.smartdesk.api.identity.service;

import java.time.Instant;
import java.util.UUID;

public record NotificationView(UUID id, UUID ticketId, String message, boolean read, Instant createdAt) { }
