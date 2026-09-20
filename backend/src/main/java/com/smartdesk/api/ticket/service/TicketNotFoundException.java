package com.smartdesk.api.ticket.service;

/**
 * Raised when a requested ticket does not exist or is not visible.
 */
public class TicketNotFoundException extends RuntimeException {

    public TicketNotFoundException() {
        super("The requested ticket could not be found.");
    }
}