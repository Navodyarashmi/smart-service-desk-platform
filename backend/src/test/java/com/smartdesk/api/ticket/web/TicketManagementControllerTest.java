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
import com.smartdesk.api.ticket.service.CreatedTicket;
import com.smartdesk.api.ticket.service.TicketCreationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class TicketManagementControllerTest {

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
    void shouldReturnRequesterOwnedTicketDetails() throws Exception {
        TestIdentity identity = createIdentity("Ticket Owner");
        CreatedTicket ticket = createTicket(identity.userId());

        mockMvc.perform(get("/api/v1/tickets/{id}", ticket.id())
                        .header(
                                "Authorization",
                                "Bearer " + identity.accessToken()
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(ticket.id().toString()))
                .andExpect(jsonPath("$.description")
                        .value("Original ticket description."))
                .andExpect(jsonPath("$.requesterId")
                        .value(identity.userId().toString()));
    }

    @Test
    void shouldUpdateRequesterOwnedOpenTicket() throws Exception {
        TestIdentity identity = createIdentity("Ticket Editor");
        CreatedTicket ticket = createTicket(identity.userId());

        String requestBody = """
                {
                  "title": "Updated ticket title",
                  "description": "Updated detailed ticket description.",
                  "category": "SOFTWARE",
                  "priority": "URGENT"
                }
                """;

        mockMvc.perform(patch("/api/v1/tickets/{id}", ticket.id())
                        .header(
                                "Authorization",
                                "Bearer " + identity.accessToken()
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title")
                        .value("Updated ticket title"))
                .andExpect(jsonPath("$.category").value("SOFTWARE"))
                .andExpect(jsonPath("$.priority").value("URGENT"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void shouldCancelTicketAndRejectFurtherChanges() throws Exception {
        TestIdentity identity = createIdentity("Ticket Canceller");
        CreatedTicket ticket = createTicket(identity.userId());

        mockMvc.perform(post("/api/v1/tickets/{id}/cancel", ticket.id())
                        .header(
                                "Authorization",
                                "Bearer " + identity.accessToken()
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(post("/api/v1/tickets/{id}/cancel", ticket.id())
                        .header(
                                "Authorization",
                                "Bearer " + identity.accessToken()
                        ))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void shouldHideTicketFromDifferentRequester() throws Exception {
        TestIdentity owner = createIdentity("Actual Owner");
        TestIdentity differentUser = createIdentity("Different User");
        CreatedTicket ticket = createTicket(owner.userId());

        mockMvc.perform(get("/api/v1/tickets/{id}", ticket.id())
                        .header(
                                "Authorization",
                                "Bearer " + differentUser.accessToken()
                        ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private CreatedTicket createTicket(UUID requesterId) {
        return ticketCreationService.create(
                new CreateTicketCommand(
                        requesterId,
                        "Original ticket title",
                        "Original ticket description.",
                        TicketCategory.HARDWARE,
                        TicketPriority.MEDIUM
                )
        );
    }

    private TestIdentity createIdentity(String fullName) {
        String email = "management-" + UUID.randomUUID()
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

        IssuedToken token =
                jwtTokenService.issueAccessToken(authenticatedUser);

        return new TestIdentity(
                registeredUser.id(),
                token.accessToken()
        );
    }

    private record TestIdentity(
            UUID userId,
            String accessToken
    ) {
    }
}