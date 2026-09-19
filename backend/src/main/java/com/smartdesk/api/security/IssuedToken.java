package com.smartdesk.api.security;

import java.time.Instant;

/**
 * Access token created after successful authentication.
 */
public record IssuedToken(
        String accessToken,
        String tokenType,
        Instant expiresAt
) {
}