package com.smartdesk.api.identity.service;

import com.smartdesk.api.identity.model.RoleCode;

import java.util.UUID;

/**
 * Safe result returned after successful user registration.
 */
public record RegisteredUser(
        UUID id,
        String email,
        String fullName,
        RoleCode role
) {
}