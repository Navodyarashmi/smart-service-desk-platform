package com.smartdesk.api.ticket.repository;

import com.smartdesk.api.identity.model.UserAccount;
import com.smartdesk.api.identity.repository.UserAccountRepository;
import com.smartdesk.api.identity.service.RegisterUserCommand;
import com.smartdesk.api.identity.service.RegisteredUser;
import com.smartdesk.api.identity.service.UserRegistrationService;
import com.smartdesk.api.ticket.model.ServiceTicket;
import com.smartdesk.api.ticket.model.TicketCategory;
import com.smartdesk.api.ticket.model.TicketPriority;
import com.smartdesk.api.ticket.model.TicketStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ServiceTicketRepositoryTest {

    @Autowired
    private ServiceTicketRepository serviceTicketRepository;

    @Autowired
    private UserRegistrationService userRegistrationService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Test
    void shouldPersistAndFindTicketForRequester() {
        RegisteredUser registeredUser =
                userRegistrationService.register(
                        new RegisterUserCommand(
                                uniqueEmail(),
                                "StrongPassword123!",
                                "Ticket Requester"
                        )
                );

        UserAccount requester = userAccountRepository
                .findById(registeredUser.id())
                .orElseThrow();

        String referenceCode = uniqueReferenceCode();

        ServiceTicket ticket = ServiceTicket.open(
                referenceCode,
                "Office Wi-Fi is unavailable",
                "The laptop cannot connect to the office Wi-Fi network.",
                TicketCategory.NETWORK,
                TicketPriority.HIGH,
                requester
        );

        ServiceTicket savedTicket =
                serviceTicketRepository.saveAndFlush(ticket);

        List<ServiceTicket> requesterTickets =
                serviceTicketRepository
                        .findAllByRequester_IdOrderByCreatedAtDesc(
                                requester.getId()
                        );

        assertNotNull(savedTicket.getId());
        assertNotNull(savedTicket.getCreatedAt());
        assertNotNull(savedTicket.getUpdatedAt());
        assertEquals(referenceCode, savedTicket.getReferenceCode());
        assertEquals(TicketStatus.OPEN, savedTicket.getStatus());
        assertEquals(requester.getId(), savedTicket.getRequester().getId());
        assertTrue(
                serviceTicketRepository.existsByReferenceCode(
                        referenceCode
                )
        );
        assertEquals(1, requesterTickets.size());
        assertEquals(savedTicket.getId(), requesterTickets.getFirst().getId());
    }

    private static String uniqueEmail() {
        return "ticket-" + UUID.randomUUID() + "@example.com";
    }

    private static String uniqueReferenceCode() {
        return "HD-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase(Locale.ROOT);
    }
}