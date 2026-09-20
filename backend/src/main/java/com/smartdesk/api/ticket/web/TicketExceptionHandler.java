package com.smartdesk.api.ticket.web;

import com.smartdesk.api.common.web.ApiErrorResponse;
import com.smartdesk.api.ticket.service.TicketNotFoundException;
import com.smartdesk.api.ticket.service.TicketStateConflictException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

/**
 * Converts ticket-management failures into REST responses.
 */
@RestControllerAdvice
public class TicketExceptionHandler {

    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            TicketNotFoundException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        return ResponseEntity.status(status).body(
                new ApiErrorResponse(
                        Instant.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        exception.getMessage(),
                        request.getRequestURI(),
                        Map.of()
                )
        );
    }

    @ExceptionHandler(TicketStateConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleStateConflict(
            TicketStateConflictException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.CONFLICT;

        return ResponseEntity.status(status).body(
                new ApiErrorResponse(
                        Instant.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        exception.getMessage(),
                        request.getRequestURI(),
                        Map.of()
                )
        );
    }
}