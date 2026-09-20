package com.smartdesk.api.ticket.service;

import com.smartdesk.api.ticket.model.TicketCategory;
import com.smartdesk.api.ticket.model.TicketPriority;
import com.smartdesk.api.ticket.model.TicketStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Safe ticket details returned after creation.
 */
public record CreatedTicket(
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