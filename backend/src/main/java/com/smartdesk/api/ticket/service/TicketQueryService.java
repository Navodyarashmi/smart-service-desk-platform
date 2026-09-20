package com.smartdesk.api.ticket.service;

import com.smartdesk.api.ticket.model.ServiceTicket;
import com.smartdesk.api.ticket.repository.ServiceTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Retrieves tickets visible to an individual requester.
 */
@Service
public class TicketQueryService {

    private final ServiceTicketRepository serviceTicketRepository;

    public TicketQueryService(
            ServiceTicketRepository serviceTicketRepository
    ) {
        this.serviceTicketRepository = serviceTicketRepository;
    }

    @Transactional(readOnly = true)
    public List<TicketSummary> findForRequester(UUID requesterId) {
        Objects.requireNonNull(
                requesterId,
                "Requester ID must not be null."
        );

        return serviceTicketRepository
                .findAllByRequester_IdOrderByCreatedAtDesc(requesterId)
                .stream()
                .map(TicketQueryService::toSummary)
                .toList();
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
}