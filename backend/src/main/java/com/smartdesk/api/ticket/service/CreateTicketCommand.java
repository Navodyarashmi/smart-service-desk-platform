package com.smartdesk.api.ticket.service;

import com.smartdesk.api.ticket.model.TicketCategory;
import com.smartdesk.api.ticket.model.TicketPriority;

import java.util.UUID;

/**
 * Information required to open a service desk ticket.
 */
public record CreateTicketCommand(
        UUID requesterId,
        String title,
        String description,
        TicketCategory category,
        TicketPriority priority
) {
}