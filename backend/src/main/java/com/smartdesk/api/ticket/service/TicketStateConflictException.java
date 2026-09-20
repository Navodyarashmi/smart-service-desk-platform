package com.smartdesk.api.ticket.service;

/**
 * Raised when the current ticket state prevents an operation.
 */
public class TicketStateConflictException extends RuntimeException {

    public TicketStateConflictException(String message) {
        super(message);
    }
}