package com.smartdesk.api.ticket.web;

import com.smartdesk.api.ticket.model.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record TicketStatusUpdateRequest(
        @NotNull(message = "Status is required.")
        TicketStatus status
) {
}
