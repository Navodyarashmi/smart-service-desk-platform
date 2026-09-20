package com.smartdesk.api.ticket.web;

import java.time.Instant;
import java.util.UUID;

public record TicketAttachmentResponse(
        UUID id,
        String filename,
        String contentType,
        long sizeBytes,
        UUID uploaderId,
        String uploaderName,
        Instant createdAt
) {
}
