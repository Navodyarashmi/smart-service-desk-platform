package com.smartdesk.api.ticket.web;

import com.smartdesk.api.ticket.service.CreateTicketCommand;
import com.smartdesk.api.ticket.service.CreatedTicket;
import com.smartdesk.api.ticket.service.TicketCreationService;
import com.smartdesk.api.ticket.service.TicketDetails;
import com.smartdesk.api.ticket.service.TicketManagementService;
import com.smartdesk.api.ticket.service.TicketQueryService;
import com.smartdesk.api.ticket.service.UpdateTicketCommand;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final TicketManagementService ticketManagementService;

    public TicketController(
            TicketCreationService ticketCreationService,
            TicketQueryService ticketQueryService,
            TicketManagementService ticketManagementService
    ) {
        this.ticketCreationService = ticketCreationService;
        this.ticketQueryService = ticketQueryService;
        this.ticketManagementService = ticketManagementService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody TicketCreationRequest request
    ) {
        UUID requesterId = requesterId(jwt);

        CreatedTicket ticket = ticketCreationService.create(
                new CreateTicketCommand(
                        requesterId,
                        request.title(),
                        request.description(),
                        request.category(),
                        request.priority()
                )
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new TicketResponse(
                        ticket.id(),
                        ticket.referenceCode(),
                        ticket.title(),
                        ticket.description(),
                        ticket.category(),
                        ticket.priority(),
                        ticket.status(),
                        ticket.requesterId(),
                        ticket.createdAt()
                ));
    }

    @GetMapping
    public List<TicketSummaryResponse> listTickets(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ticketQueryService
                .findForRequester(requesterId(jwt))
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

    @GetMapping("/{ticketId}")
    public TicketDetailsResponse getTicket(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ticketId
    ) {
        return toDetailsResponse(
                ticketManagementService.findDetails(
                        ticketId,
                        requesterId(jwt)
                )
        );
    }

    @PatchMapping("/{ticketId}")
    public TicketDetailsResponse updateTicket(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ticketId,
            @Valid @RequestBody TicketUpdateRequest request
    ) {
        TicketDetails details = ticketManagementService.update(
                new UpdateTicketCommand(
                        ticketId,
                        requesterId(jwt),
                        request.title(),
                        request.description(),
                        request.category(),
                        request.priority()
                )
        );

        return toDetailsResponse(details);
    }

    @PostMapping("/{ticketId}/cancel")
    public TicketDetailsResponse cancelTicket(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ticketId
    ) {
        return toDetailsResponse(
                ticketManagementService.cancel(
                        ticketId,
                        requesterId(jwt)
                )
        );
    }

    private static UUID requesterId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    private static TicketDetailsResponse toDetailsResponse(
            TicketDetails ticket
    ) {
        return new TicketDetailsResponse(
                ticket.id(),
                ticket.referenceCode(),
                ticket.title(),
                ticket.description(),
                ticket.category(),
                ticket.priority(),
                ticket.status(),
                ticket.requesterId(),
                ticket.assigneeId(),
                ticket.createdAt(),
                ticket.updatedAt(),
                ticket.resolvedAt()
        );
    }
}