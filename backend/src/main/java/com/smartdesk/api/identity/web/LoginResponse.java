package com.smartdesk.api.identity.web;

import com.smartdesk.api.identity.model.RoleCode;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Safe login result returned to the frontend.
 */
public record LoginResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        UUID userId,
        String email,
        String fullName,
        Set<RoleCode> roles
) {
}