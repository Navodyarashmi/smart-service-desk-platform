package com.smartdesk.api.identity.service;

/**
 * Raised when submitted login credentials are invalid.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Email or password is incorrect.");
    }
}