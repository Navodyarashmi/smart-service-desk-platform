package com.smartdesk.api.identity.service;

/**
 * Input required to register a new user account.
 */
public record RegisterUserCommand(
        String email,
        String password,
        String fullName
) {
}