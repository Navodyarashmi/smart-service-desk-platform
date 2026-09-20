package com.smartdesk.api.ticket.web;

import com.smartdesk.api.ticket.service.StaffTicketService;
import com.smartdesk.api.ticket.service.TicketDetails;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * Role-protected service desk operations for technicians and administrators.
 */
@RestController
@RequestMapping("/api/v1/staff/tickets")
@PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMINISTRATOR')")
public class StaffTicketController {

    private final StaffTicketService staffTicketService;

    public StaffTicketController(StaffTicketService staffTicketService) {
        this.staffTicketService = staffTicketService;
    }

    @GetMapping
    public List<TicketSummaryResponse> listWorkQueue() {
        return staffTicketService.findWorkQueue()
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
    public TicketDetailsResponse getTicket(@PathVariable UUID ticketId) {
        return toResponse(staffTicketService.findDetails(ticketId));
    }

    @PostMapping("/{ticketId}/claim")
    public TicketDetailsResponse claimTicket(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ticketId
    ) {
        return toResponse(staffTicketService.claim(ticketId, userId(jwt)));
    }

    @PatchMapping("/{ticketId}/status")
    public TicketDetailsResponse changeStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ticketId,
            @Valid @RequestBody TicketStatusUpdateRequest request
    ) {
        return toResponse(staffTicketService.changeStatus(
                ticketId,
                userId(jwt),
                request.status()
        ));
    }

    private static UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    private static TicketDetailsResponse toResponse(TicketDetails ticket) {
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
