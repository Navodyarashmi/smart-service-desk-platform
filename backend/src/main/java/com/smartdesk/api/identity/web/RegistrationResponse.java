package com.smartdesk.api.identity.web;

import com.smartdesk.api.identity.model.RoleCode;

import java.util.UUID;

/**
 * Safe JSON returned after successful registration.
 */
public record RegistrationResponse(
        UUID id,
        String email,
        String fullName,
        RoleCode role
) {
}