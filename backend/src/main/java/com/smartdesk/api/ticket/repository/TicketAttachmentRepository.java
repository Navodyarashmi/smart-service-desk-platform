package com.smartdesk.api.ticket.repository;

import com.smartdesk.api.ticket.model.TicketAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketAttachmentRepository
        extends JpaRepository<TicketAttachment, UUID> {

    List<TicketAttachment> findAllByTicket_IdOrderByCreatedAtAsc(UUID ticketId);

    Optional<TicketAttachment> findByIdAndTicket_Id(UUID id, UUID ticketId);
}
