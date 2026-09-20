package com.smartdesk.api.ticket.web;

import com.smartdesk.api.ticket.model.TicketCategory;
import com.smartdesk.api.ticket.model.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * JSON accepted when an authenticated user creates a ticket.
 */
public record TicketCreationRequest(

        @NotBlank(message = "Title is required.")
        @Size(max = 160, message = "Title must not exceed 160 characters.")
        String title,

        @NotBlank(message = "Description is required.")
        @Size(
                max = 4000,
                message = "Description must not exceed 4000 characters."
        )
        String description,

        @NotNull(message = "Category is required.")
        TicketCategory category,

        @NotNull(message = "Priority is required.")
        TicketPriority priority
) {
}