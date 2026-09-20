package com.smartdesk.api.ticket.web;

import com.smartdesk.api.ticket.service.CreateTicketCommand;
import com.smartdesk.api.ticket.service.CreatedTicket;
import com.smartdesk.api.ticket.service.TicketCreationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Provides authenticated ticket operations.
 */
@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketCreationService ticketCreationService;

    public TicketController(
            TicketCreationService ticketCreationService
    ) {
        this.ticketCreationService = ticketCreationService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody TicketCreationRequest request
    ) {
        UUID requesterId = UUID.fromString(jwt.getSubject());

        CreatedTicket createdTicket = ticketCreationService.create(
                new CreateTicketCommand(
                        requesterId,
                        request.title(),
                        request.description(),
                        request.category(),
                        request.priority()
                )
        );

        TicketResponse response = new TicketResponse(
                createdTicket.id(),
                createdTicket.referenceCode(),
                createdTicket.title(),
                createdTicket.description(),
                createdTicket.category(),
                createdTicket.priority(),
                createdTicket.status(),
                createdTicket.requesterId(),
                createdTicket.createdAt()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}