package com.smartdesk.api.ticket.service;

import com.smartdesk.api.ticket.model.TicketCategory;
import com.smartdesk.api.ticket.model.TicketPriority;

import java.util.UUID;

/**
 * Changes submitted by a ticket requester.
 */
public record UpdateTicketCommand(
        UUID ticketId,
        UUID requesterId,
        String title,
        String description,
        TicketCategory category,
        TicketPriority priority
) {
}