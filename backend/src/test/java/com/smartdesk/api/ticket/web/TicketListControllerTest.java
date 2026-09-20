package com.smartdesk.api.ticket.web;

import com.smartdesk.api.identity.service.AuthenticatedUser;
import com.smartdesk.api.identity.service.LoginCommand;
import com.smartdesk.api.identity.service.RegisterUserCommand;
import com.smartdesk.api.identity.service.RegisteredUser;
import com.smartdesk.api.identity.service.UserAuthenticationService;
import com.smartdesk.api.identity.service.UserRegistrationService;
import com.smartdesk.api.security.IssuedToken;
import com.smartdesk.api.security.JwtTokenService;
import com.smartdesk.api.ticket.model.TicketCategory;
import com.smartdesk.api.ticket.model.TicketPriority;
import com.smartdesk.api.ticket.service.CreateTicketCommand;
import com.smartdesk.api.ticket.service.TicketCreationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class TicketListControllerTest {

    private static final String VALID_PASSWORD = "StrongPassword123!";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRegistrationService userRegistrationService;

    @Autowired
    private UserAuthenticationService userAuthenticationService;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private TicketCreationService ticketCreationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void shouldListOnlyAuthenticatedRequestersTickets() throws Exception {
        TestIdentity currentUser = createIdentity("Current Requester");
        TestIdentity anotherUser = createIdentity("Another Requester");

        ticketCreationService.create(
                new CreateTicketCommand(
                        currentUser.userId(),
                        "Current user's network ticket",
                        "The office network is unavailable.",
                        TicketCategory.NETWORK,
                        TicketPriority.HIGH
                )
        );

        ticketCreationService.create(
                new CreateTicketCommand(
                        anotherUser.userId(),
                        "Another user's private ticket",
                        "This ticket must not be visible.",
                        TicketCategory.ACCESS,
                        TicketPriority.LOW
                )
        );

        mockMvc.perform(get("/api/v1/tickets")
                        .header(
                                "Authorization",
                                "Bearer " + currentUser.accessToken()
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").isNotEmpty())
                .andExpect(jsonPath("$[0].referenceCode")
                        .value(org.hamcrest.Matchers
                                .matchesPattern("HD-[A-F0-9]{16}")))
                .andExpect(jsonPath("$[0].title")
                        .value("Current user's network ticket"))
                .andExpect(jsonPath("$[0].category").value("NETWORK"))
                .andExpect(jsonPath("$[0].priority").value("HIGH"))
                .andExpect(jsonPath("$[0].status").value("OPEN"))
                .andExpect(jsonPath("$[0].createdAt").isNotEmpty())
                .andExpect(jsonPath("$[0].updatedAt").isNotEmpty());
    }

    private TestIdentity createIdentity(String fullName) {
        String email = "ticket-list-" + UUID.randomUUID()
                + "@example.com";

        RegisteredUser registeredUser =
                userRegistrationService.register(
                        new RegisterUserCommand(
                                email,
                                VALID_PASSWORD,
                                fullName
                        )
                );

        AuthenticatedUser authenticatedUser =
                userAuthenticationService.authenticate(
                        new LoginCommand(email, VALID_PASSWORD)
                );

        IssuedToken issuedToken =
                jwtTokenService.issueAccessToken(authenticatedUser);

        return new TestIdentity(
                registeredUser.id(),
                issuedToken.accessToken()
        );
    }

    private record TestIdentity(
            UUID userId,
            String accessToken
    ) {
    }
}