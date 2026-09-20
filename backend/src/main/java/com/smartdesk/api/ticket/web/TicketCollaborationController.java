package com.smartdesk.api.ticket.web;

import com.smartdesk.api.ticket.service.TicketActivityView;
import com.smartdesk.api.ticket.service.TicketCollaborationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets/{ticketId}/activity")
public class TicketCollaborationController {
    private final TicketCollaborationService service;
    public TicketCollaborationController(TicketCollaborationService service) { this.service = service; }

    @GetMapping
    public List<TicketActivityResponse> list(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID ticketId) {
        return service.list(ticketId, userId(jwt), isStaff(jwt)).stream().map(TicketCollaborationController::response).toList();
    }

    @PostMapping
    public TicketActivityResponse comment(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID ticketId, @Valid @RequestBody TicketCommentRequest request) {
        return response(service.comment(ticketId, userId(jwt), isStaff(jwt), request.message(), request.internalNote()));
    }

    private static UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
    private static boolean isStaff(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles != null && (roles.contains("TECHNICIAN") || roles.contains("ADMINISTRATOR"));
    }
    private static TicketActivityResponse response(TicketActivityView item) {
        return new TicketActivityResponse(item.id(), item.type(), item.message(), item.internalNote(), item.actorId(), item.actorName(), item.createdAt());
    }
}
