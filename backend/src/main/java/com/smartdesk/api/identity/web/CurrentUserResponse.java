package com.smartdesk.api.identity.web;

import java.util.List;
import java.util.UUID;

/**
 * Safe information about the currently authenticated user.
 */
public record CurrentUserResponse(
        UUID id,
        String email,
        String fullName,
        List<String> roles
) {
}