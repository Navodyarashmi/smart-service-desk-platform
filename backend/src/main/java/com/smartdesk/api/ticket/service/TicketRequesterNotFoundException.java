package com.smartdesk.api.ticket.service;

/**
 * Raised when a ticket requester no longer exists.
 */
public class TicketRequesterNotFoundException extends RuntimeException {

    public TicketRequesterNotFoundException() {
        super("The ticket requester could not be found.");
    }
}