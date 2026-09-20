package com.smartdesk.api.ticket.service;

import com.smartdesk.api.identity.model.RoleCode;
import com.smartdesk.api.identity.model.UserAccount;
import com.smartdesk.api.identity.repository.UserAccountRepository;
import com.smartdesk.api.identity.repository.UserRoleAssignmentRepository;
import com.smartdesk.api.ticket.model.ServiceTicket;
import com.smartdesk.api.ticket.model.TicketStatus;
import com.smartdesk.api.ticket.repository.ServiceTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Provides the technician queue and guarded ticket lifecycle actions.
 */
@Service
public class StaffTicketService {

    private final ServiceTicketRepository ticketRepository;
    private final UserAccountRepository userRepository;
    private final UserRoleAssignmentRepository roleAssignmentRepository;

    public StaffTicketService(
            ServiceTicketRepository ticketRepository,
            UserAccountRepository userRepository,
            UserRoleAssignmentRepository roleAssignmentRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
    }

    @Transactional(readOnly = true)
    public List<TicketSummary> findWorkQueue() {
        return ticketRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(StaffTicketService::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketDetails findDetails(UUID ticketId) {
        return toDetails(findTicket(ticketId));
    }

    @Transactional
    public TicketDetails claim(UUID ticketId, UUID staffUserId) {
        ServiceTicket ticket = findTicket(ticketId);
        UserAccount technician = findStaffUser(staffUserId);

        if (ticket.getStatus() == TicketStatus.CLOSED
                || ticket.getStatus() == TicketStatus.CANCELLED
                || ticket.getStatus() == TicketStatus.RESOLVED) {
            throw new TicketStateConflictException(
                    "A completed or cancelled ticket cannot be claimed."
            );
        }

        if (ticket.getAssignee() != null
                && !ticket.getAssignee().getId().equals(staffUserId)) {
            throw new TicketStateConflictException(
                    "This ticket is already assigned to another technician."
            );
        }

        if (ticket.getAssignee() != null) {
            return toDetails(ticket);
        }

        ticket.assignTo(technician);
        return toDetails(ticketRepository.saveAndFlush(ticket));
    }

    @Transactional
    public TicketDetails changeStatus(
            UUID ticketId,
            UUID staffUserId,
            TicketStatus requestedStatus
    ) {
        Objects.requireNonNull(
                requestedStatus,
                "Requested status must not be null."
        );

        ServiceTicket ticket = findTicket(ticketId);
        findStaffUser(staffUserId);

        if (ticket.getAssignee() == null
                || !ticket.getAssignee().getId().equals(staffUserId)) {
            throw new TicketStateConflictException(
                    "Claim the ticket before changing its status."
            );
        }

        TicketStatus currentStatus = ticket.getStatus();

        if (currentStatus == TicketStatus.ASSIGNED
                && requestedStatus == TicketStatus.IN_PROGRESS) {
            ticket.beginProgress();
        } else if (currentStatus == TicketStatus.IN_PROGRESS
                && requestedStatus == TicketStatus.RESOLVED) {
            ticket.resolve();
        } else if (currentStatus == TicketStatus.RESOLVED
                && requestedStatus == TicketStatus.CLOSED) {
            ticket.close();
        } else {
            throw new TicketStateConflictException(
                    "Invalid ticket transition from " + currentStatus
                            + " to " + requestedStatus + "."
            );
        }

        return toDetails(ticketRepository.saveAndFlush(ticket));
    }

    private ServiceTicket findTicket(UUID ticketId) {
        Objects.requireNonNull(ticketId, "Ticket ID must not be null.");
        return ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);
    }

    private UserAccount findStaffUser(UUID userId) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(TicketRequesterNotFoundException::new);
        boolean isStaff = roleAssignmentRepository
                .existsByUser_IdAndRole_Code(userId, RoleCode.TECHNICIAN)
                || roleAssignmentRepository.existsByUser_IdAndRole_Code(
                        userId,
                        RoleCode.ADMINISTRATOR
                );

        if (!isStaff) {
            throw new TicketStateConflictException(
                    "Only service desk staff can manage the work queue."
            );
        }

        return user;
    }

    private static TicketSummary toSummary(ServiceTicket ticket) {
        return new TicketSummary(
                ticket.getId(),
                ticket.getReferenceCode(),
                ticket.getTitle(),
                ticket.getCategory(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
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
