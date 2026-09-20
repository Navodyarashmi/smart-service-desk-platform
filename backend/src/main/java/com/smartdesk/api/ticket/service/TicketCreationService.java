package com.smartdesk.api.ticket.service;

import com.smartdesk.api.identity.model.UserAccount;
import com.smartdesk.api.identity.repository.UserAccountRepository;
import com.smartdesk.api.ticket.model.ServiceTicket;
import com.smartdesk.api.ticket.model.TicketActivityType;
import com.smartdesk.api.ticket.repository.ServiceTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Opens tickets on behalf of authenticated users.
 */
@Service
public class TicketCreationService {

    private final ServiceTicketRepository serviceTicketRepository;
    private final UserAccountRepository userAccountRepository;
    private final TicketCollaborationService collaborationService;

    public TicketCreationService(
            ServiceTicketRepository serviceTicketRepository,
            UserAccountRepository userAccountRepository,
            TicketCollaborationService collaborationService
    ) {
        this.serviceTicketRepository = serviceTicketRepository;
        this.userAccountRepository = userAccountRepository;
        this.collaborationService = collaborationService;
    }

    @Transactional
    public CreatedTicket create(CreateTicketCommand command) {
        Objects.requireNonNull(
                command,
                "Create ticket command must not be null."
        );

        UUID requesterId = Objects.requireNonNull(
                command.requesterId(),
                "Requester ID must not be null."
        );

        UserAccount requester = userAccountRepository
                .findById(requesterId)
                .orElseThrow(TicketRequesterNotFoundException::new);

        ServiceTicket ticket = ServiceTicket.open(
                generateReferenceCode(),
                command.title(),
                command.description(),
                command.category(),
                command.priority(),
                requester
        );

        ServiceTicket savedTicket =
                serviceTicketRepository.saveAndFlush(ticket);
        collaborationService.audit(
                savedTicket,
                requester,
                TicketActivityType.CREATED,
                "Ticket created"
        );

        return new CreatedTicket(
                savedTicket.getId(),
                savedTicket.getReferenceCode(),
                savedTicket.getTitle(),
                savedTicket.getDescription(),
                savedTicket.getCategory(),
                savedTicket.getPriority(),
                savedTicket.getStatus(),
                savedTicket.getRequester().getId(),
                savedTicket.getCreatedAt()
        );
    }

    private static String generateReferenceCode() {
        String randomPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 16)
                .toUpperCase(Locale.ROOT);

        return "HD-" + randomPart;
    }
}
