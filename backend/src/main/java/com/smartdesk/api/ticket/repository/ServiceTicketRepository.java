package com.smartdesk.api.ticket.repository;

import com.smartdesk.api.ticket.model.ServiceTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceTicketRepository
        extends JpaRepository<ServiceTicket, UUID> {

    List<ServiceTicket> findAllByRequester_IdOrderByCreatedAtDesc(
            UUID requesterId
    );

    Optional<ServiceTicket> findByIdAndRequester_Id(
            UUID ticketId,
            UUID requesterId
    );

    boolean existsByReferenceCode(String referenceCode);
}