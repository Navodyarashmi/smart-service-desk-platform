package com.smartdesk.api.identity.service;

/**
 * Raised when a valid account cannot currently be used.
 */
public class AccountUnavailableException extends RuntimeException {

    public AccountUnavailableException() {
        super("This account is currently unavailable.");
    }
}