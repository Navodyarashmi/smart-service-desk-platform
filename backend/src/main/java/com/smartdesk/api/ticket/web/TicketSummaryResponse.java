package com.smartdesk.api.ticket.web;

import com.smartdesk.api.ticket.model.TicketCategory;
import com.smartdesk.api.ticket.model.TicketPriority;
import com.smartdesk.api.ticket.model.TicketStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Compact ticket JSON returned to dashboard clients.
 */
public record TicketSummaryResponse(
        UUID id,
        String referenceCode,
        String title,
        TicketCategory category,
        TicketPriority priority,
        TicketStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}