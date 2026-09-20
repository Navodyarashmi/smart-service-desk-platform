package com.smartdesk.api.identity.service;

import com.smartdesk.api.identity.model.RoleCode;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AdminUserSummary(
        UUID id,
        String email,
        String fullName,
        Set<RoleCode> roles,
        boolean enabled,
        boolean accountLocked,
        Instant createdAt
) {
}
