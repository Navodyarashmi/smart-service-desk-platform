package com.smartdesk.api.ticket.service;

import com.smartdesk.api.identity.service.RegisterUserCommand;
import com.smartdesk.api.identity.service.RegisteredUser;
import com.smartdesk.api.identity.service.UserRegistrationService;
import com.smartdesk.api.ticket.model.ServiceTicket;
import com.smartdesk.api.ticket.model.TicketCategory;
import com.smartdesk.api.ticket.model.TicketPriority;
import com.smartdesk.api.ticket.model.TicketStatus;
import com.smartdesk.api.ticket.repository.ServiceTicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class TicketCreationServiceTest {

    @Autowired
    private TicketCreationService ticketCreationService;

    @Autowired
    private UserRegistrationService userRegistrationService;

    @Autowired
    private ServiceTicketRepository serviceTicketRepository;

    @Test
    void shouldCreateOpenTicketForRequester() {
        RegisteredUser requester = registerRequester();

        CreatedTicket createdTicket = ticketCreationService.create(
                new CreateTicketCommand(
                        requester.id(),
                        "Email application is unavailable",
                        "The application displays an error after sign-in.",
                        TicketCategory.SOFTWARE,
                        TicketPriority.HIGH
                )
        );

        ServiceTicket savedTicket = serviceTicketRepository
                .findById(createdTicket.id())
                .orElseThrow();

        assertNotNull(createdTicket.id());
        assertNotNull(createdTicket.createdAt());
        assertTrue(
                createdTicket.referenceCode()
                        .matches("HD-[A-F0-9]{16}")
        );
        assertEquals(
                TicketStatus.OPEN,
                createdTicket.status()
        );
        assertEquals(
                requester.id(),
                createdTicket.requesterId()
        );
        assertEquals(
                requester.id(),
                savedTicket.getRequester().getId()
        );
        assertEquals(
                TicketCategory.SOFTWARE,
                savedTicket.getCategory()
        );
        assertEquals(
                TicketPriority.HIGH,
                savedTicket.getPriority()
        );
    }

    @Test
    void shouldRejectUnknownRequester() {
        assertThrows(
                TicketRequesterNotFoundException.class,
                () -> ticketCreationService.create(
                        new CreateTicketCommand(
                                UUID.randomUUID(),
                                "Test ticket",
                                "Test ticket description.",
                                TicketCategory.OTHER,
                                TicketPriority.LOW
                        )
                )
        );
    }

    private RegisteredUser registerRequester() {
        return userRegistrationService.register(
                new RegisterUserCommand(
                        "requester-" + UUID.randomUUID() + "@example.com",
                        "StrongPassword123!",
                        "Ticket Service Requester"
                )
        );
    }
}