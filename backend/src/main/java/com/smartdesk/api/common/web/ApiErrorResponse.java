package com.smartdesk.api.common.web;

import java.time.Instant;
import java.util.Map;

/**
 * Standard error body returned by REST endpoints.
 */
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}