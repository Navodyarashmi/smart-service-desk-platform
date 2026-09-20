package com.smartdesk.api.ticket.service;

import com.smartdesk.api.ticket.model.TicketCategory;
import com.smartdesk.api.ticket.model.TicketPriority;
import com.smartdesk.api.ticket.model.TicketStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Compact ticket information used in lists and dashboards.
 */
public record TicketSummary(
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