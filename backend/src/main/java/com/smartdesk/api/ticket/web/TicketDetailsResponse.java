package com.smartdesk.api.ticket.web;

import com.smartdesk.api.ticket.model.TicketCategory;
import com.smartdesk.api.ticket.model.TicketPriority;
import com.smartdesk.api.ticket.model.TicketStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Complete ticket JSON visible to its requester.
 */
public record TicketDetailsResponse(
        UUID id,
        String referenceCode,
        String title,
        String description,
        TicketCategory category,
        TicketPriority priority,
        TicketStatus status,
        UUID requesterId,
        UUID assigneeId,
        Instant createdAt,
        Instant updatedAt,
        Instant resolvedAt
) {
}