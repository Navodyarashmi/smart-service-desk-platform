package com.smartdesk.api.common.web;

import com.smartdesk.api.identity.service.AccountUnavailableException;
import com.smartdesk.api.identity.service.AdminActionConflictException;
import com.smartdesk.api.identity.service.EmailAlreadyRegisteredException;
import com.smartdesk.api.identity.service.InvalidCredentialsException;
import com.smartdesk.api.identity.service.NotificationNotFoundException;
import com.smartdesk.api.ticket.service.TicketRequesterNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts expected application errors into consistent HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingNotification(
            NotificationNotFoundException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        ));
    }

    @ExceptionHandler(AdminActionConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleAdminConflict(
            AdminActionConflictException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.CONFLICT;
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        ));
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateEmail(
            EmailAlreadyRegisteredException exception,
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationFailure(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        for (FieldError fieldError :
                exception.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(
                new ApiErrorResponse(
                        Instant.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        "Request validation failed.",
                        request.getRequestURI(),
                        fieldErrors
                )
        );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

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

    @ExceptionHandler(AccountUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnavailableAccount(
            AccountUnavailableException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.FORBIDDEN;

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

    @ExceptionHandler(TicketRequesterNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingRequester(
            TicketRequesterNotFoundException exception,
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
}
