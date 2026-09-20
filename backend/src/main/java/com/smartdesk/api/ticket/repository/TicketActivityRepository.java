package com.smartdesk.api.ticket.repository;

import com.smartdesk.api.ticket.model.TicketActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketActivityRepository extends JpaRepository<TicketActivity, UUID> {
    List<TicketActivity> findAllByTicket_IdOrderByCreatedAtAsc(UUID ticketId);
}
