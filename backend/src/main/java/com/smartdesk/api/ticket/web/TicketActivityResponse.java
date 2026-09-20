package com.smartdesk.api.ticket.web;

import com.smartdesk.api.ticket.model.TicketActivityType;

import java.time.Instant;
import java.util.UUID;

public record TicketActivityResponse(UUID id, TicketActivityType type, String message, boolean internalNote, UUID actorId, String actorName, Instant createdAt) {
}
