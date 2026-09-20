package com.smartdesk.api.identity.web;

import com.smartdesk.api.identity.model.RoleCode;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String email,
        String fullName,
        Set<RoleCode> roles,
        boolean enabled,
        boolean accountLocked,
        Instant createdAt
) {
}
