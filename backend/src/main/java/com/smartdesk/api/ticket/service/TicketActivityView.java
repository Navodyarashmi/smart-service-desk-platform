package com.smartdesk.api.ticket.service;

import com.smartdesk.api.ticket.model.TicketActivityType;

import java.time.Instant;
import java.util.UUID;

public record TicketActivityView(
        UUID id,
        TicketActivityType type,
        String message,
        boolean internalNote,
        UUID actorId,
        String actorName,
        Instant createdAt
) {
}
