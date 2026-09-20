package com.smartdesk.api.ticket.web;

import com.smartdesk.api.identity.service.AuthenticatedUser;
import com.smartdesk.api.identity.service.LoginCommand;
import com.smartdesk.api.identity.service.RegisterUserCommand;
import com.smartdesk.api.identity.service.UserAuthenticationService;
import com.smartdesk.api.identity.service.UserRegistrationService;
import com.smartdesk.api.security.IssuedToken;
import com.smartdesk.api.security.JwtTokenService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class TicketControllerTest {

    private static final String VALID_PASSWORD = "StrongPassword123!";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRegistrationService userRegistrationService;

    @Autowired
    private UserAuthenticationService userAuthenticationService;

    @Autowired
    private JwtTokenService jwtTokenService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void shouldCreateTicketForAuthenticatedUser() throws Exception {
        TestIdentity identity = createIdentity();

        String requestBody = """
                {
                  "title": "Office Wi-Fi is unavailable",
                  "description": "The laptop cannot connect to Wi-Fi.",
                  "category": "NETWORK",
                  "priority": "HIGH"
                }
                """;

        mockMvc.perform(post("/api/v1/tickets")
                        .header(
                                "Authorization",
                                "Bearer " + identity.accessToken()
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.referenceCode")
                        .value(org.hamcrest.Matchers
                                .matchesPattern("HD-[A-F0-9]{16}")))
                .andExpect(jsonPath("$.title")
                        .value("Office Wi-Fi is unavailable"))
                .andExpect(jsonPath("$.category").value("NETWORK"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.requesterId")
                        .value(identity.userId().toString()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void shouldRejectTicketWithoutAccessToken() throws Exception {
        String requestBody = """
                {
                  "title": "Test ticket",
                  "description": "Test description.",
                  "category": "OTHER",
                  "priority": "LOW"
                }
                """;

        mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectInvalidTicketInput() throws Exception {
        TestIdentity identity = createIdentity();

        String requestBody = """
                {
                  "title": " ",
                  "description": " ",
                  "category": null,
                  "priority": null
                }
                """;

        mockMvc.perform(post("/api/v1/tickets")
                        .header(
                                "Authorization",
                                "Bearer " + identity.accessToken()
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Request validation failed."))
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.description").exists())
                .andExpect(jsonPath("$.fieldErrors.category").exists())
                .andExpect(jsonPath("$.fieldErrors.priority").exists());
    }

    private TestIdentity createIdentity() {
        String email = "ticket-web-" + UUID.randomUUID()
                + "@example.com";

        UUID userId = userRegistrationService.register(
                new RegisterUserCommand(
                        email,
                        VALID_PASSWORD,
                        "Ticket Web User"
                )
        ).id();

        AuthenticatedUser authenticatedUser =
                userAuthenticationService.authenticate(
                        new LoginCommand(email, VALID_PASSWORD)
                );

        IssuedToken issuedToken =
                jwtTokenService.issueAccessToken(authenticatedUser);

        return new TestIdentity(
                userId,
                issuedToken.accessToken()
        );
    }

    private record TestIdentity(
            UUID userId,
            String accessToken
    ) {
    }
}