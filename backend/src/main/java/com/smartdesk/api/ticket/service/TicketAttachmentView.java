package com.smartdesk.api.ticket.service;

import java.time.Instant;
import java.util.UUID;

public record TicketAttachmentView(
        UUID id,
        String filename,
        String contentType,
        long sizeBytes,
        UUID uploaderId,
        String uploaderName,
        Instant createdAt
) {
}
