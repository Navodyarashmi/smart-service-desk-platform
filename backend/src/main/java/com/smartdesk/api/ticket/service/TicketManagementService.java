package com.smartdesk.api.ticket.service;

import com.smartdesk.api.ticket.model.ServiceTicket;
import com.smartdesk.api.ticket.model.TicketActivityType;
import com.smartdesk.api.ticket.model.TicketStatus;
import com.smartdesk.api.ticket.repository.ServiceTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

/**
 * Manages requester-owned ticket details and lifecycle operations.
 */
@Service
public class TicketManagementService {

    private final ServiceTicketRepository serviceTicketRepository;
    private final TicketCollaborationService collaborationService;

    public TicketManagementService(
            ServiceTicketRepository serviceTicketRepository,
            TicketCollaborationService collaborationService
    ) {
        this.serviceTicketRepository = serviceTicketRepository;
        this.collaborationService = collaborationService;
    }

    @Transactional(readOnly = true)
    public TicketDetails findDetails(
            UUID ticketId,
            UUID requesterId
    ) {
        return toDetails(findOwnedTicket(ticketId, requesterId));
    }

    @Transactional
    public TicketDetails update(UpdateTicketCommand command) {
        Objects.requireNonNull(
                command,
                "Update ticket command must not be null."
        );

        ServiceTicket ticket = findOwnedTicket(
                command.ticketId(),
                command.requesterId()
        );

        ensureRequesterCanModify(ticket);

        ticket.updateDetails(
                command.title(),
                command.description(),
                command.category(),
                command.priority()
        );

        ServiceTicket saved = serviceTicketRepository.saveAndFlush(ticket);
        collaborationService.audit(saved, saved.getRequester(), TicketActivityType.UPDATED, "Ticket details updated");
        return toDetails(saved);
    }

    @Transactional
    public TicketDetails cancel(
            UUID ticketId,
            UUID requesterId
    ) {
        ServiceTicket ticket = findOwnedTicket(ticketId, requesterId);

        ensureRequesterCanModify(ticket);
        ticket.cancel();

        ServiceTicket saved = serviceTicketRepository.saveAndFlush(ticket);
        collaborationService.audit(saved, saved.getRequester(), TicketActivityType.CANCELLED, "Ticket cancelled by requester");
        return toDetails(saved);
    }

    private ServiceTicket findOwnedTicket(
            UUID ticketId,
            UUID requesterId
    ) {
        Objects.requireNonNull(ticketId, "Ticket ID must not be null.");
        Objects.requireNonNull(
                requesterId,
                "Requester ID must not be null."
        );

        return serviceTicketRepository
                .findByIdAndRequester_Id(ticketId, requesterId)
                .orElseThrow(TicketNotFoundException::new);
    }

    private static void ensureRequesterCanModify(
            ServiceTicket ticket
    ) {
        boolean editable = ticket.getStatus() == TicketStatus.OPEN
                || ticket.getStatus() == TicketStatus.ASSIGNED;

        if (!editable) {
            throw new TicketStateConflictException(
                    "This ticket can no longer be changed by its requester."
            );
        }
    }

    private static TicketDetails toDetails(ServiceTicket ticket) {
        UUID assigneeId = ticket.getAssignee() == null
                ? null
                : ticket.getAssignee().getId();

        return new TicketDetails(
                ticket.getId(),
                ticket.getReferenceCode(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getCategory(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getRequester().getId(),
                assigneeId,
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getResolvedAt()
        );
    }
}
