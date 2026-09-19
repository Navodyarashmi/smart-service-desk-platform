package com.smartdesk.api.identity.service;

/**
 * Raised when registration uses an email belonging to an existing account.
 */
public class EmailAlreadyRegisteredException extends RuntimeException {

    public EmailAlreadyRegisteredException() {
        super("An account already exists with this email address.");
    }
}