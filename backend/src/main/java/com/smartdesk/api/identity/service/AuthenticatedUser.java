package com.smartdesk.api.identity.service;

import com.smartdesk.api.identity.model.RoleCode;

import java.util.Set;
import java.util.UUID;

/**
 * Safe user identity returned after successful authentication.
 */
public record AuthenticatedUser(
        UUID id,
        String email,
        String fullName,
        Set<RoleCode> roles
) {
}