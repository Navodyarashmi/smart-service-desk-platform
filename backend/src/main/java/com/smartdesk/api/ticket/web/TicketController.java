package com.smartdesk.api.ticket.web;

import com.smartdesk.api.ticket.service.CreateTicketCommand;
import com.smartdesk.api.ticket.service.CreatedTicket;
import com.smartdesk.api.ticket.service.TicketCreationService;
import com.smartdesk.api.ticket.service.TicketQueryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Provides authenticated ticket operations.
 */
@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketCreationService ticketCreationService;
    private final TicketQueryService ticketQueryService;

    public TicketController(
            TicketCreationService ticketCreationService,
            TicketQueryService ticketQueryService
    ) {
        this.ticketCreationService = ticketCreationService;
        this.ticketQueryService = ticketQueryService;
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

    @GetMapping
    public List<TicketSummaryResponse> listTickets(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID requesterId = UUID.fromString(jwt.getSubject());

        return ticketQueryService
                .findForRequester(requesterId)
                .stream()
                .map(ticket -> new TicketSummaryResponse(
                        ticket.id(),
                        ticket.referenceCode(),
                        ticket.title(),
                        ticket.category(),
                        ticket.priority(),
                        ticket.status(),
                        ticket.createdAt(),
                        ticket.updatedAt()
                ))
                .toList();
    }
}