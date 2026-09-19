package com.smartdesk.api.identity.service;

/**
 * Credentials submitted during login.
 */
public record LoginCommand(
        String email,
        String password
) {
}