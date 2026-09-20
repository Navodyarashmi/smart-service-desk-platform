package com.smartdesk.api.ticket.web;

import com.smartdesk.api.ticket.model.TicketCategory;
import com.smartdesk.api.ticket.model.TicketPriority;
import com.smartdesk.api.ticket.model.TicketStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Safe ticket information returned by the REST API.
 */
public record TicketResponse(
        UUID id,
        String referenceCode,
        String title,
        String description,
        TicketCategory category,
        TicketPriority priority,
        TicketStatus status,
        UUID requesterId,
        Instant createdAt
) {
}