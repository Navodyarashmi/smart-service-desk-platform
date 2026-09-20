package com.smartdesk.api.identity.web;

import com.smartdesk.api.identity.service.AuthenticatedUser;
import com.smartdesk.api.identity.service.LoginCommand;
import com.smartdesk.api.identity.service.RegisterUserCommand;
import com.smartdesk.api.identity.service.RegisteredUser;
import com.smartdesk.api.identity.service.UserAuthenticationService;
import com.smartdesk.api.identity.service.UserRegistrationService;
import com.smartdesk.api.security.IssuedToken;
import com.smartdesk.api.security.JwtTokenService;
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
class CurrentUserControllerTest {

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
    void shouldRejectRequestWithoutAccessToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnCurrentUserForValidAccessToken() throws Exception {
        String email = uniqueEmail();

        RegisteredUser registeredUser =
                userRegistrationService.register(
                        new RegisterUserCommand(
                                email,
                                VALID_PASSWORD,
                                "Current User Test"
                        )
                );

        AuthenticatedUser authenticatedUser =
                userAuthenticationService.authenticate(
                        new LoginCommand(email, VALID_PASSWORD)
                );

        IssuedToken issuedToken =
                jwtTokenService.issueAccessToken(authenticatedUser);

        mockMvc.perform(get("/api/v1/users/me")
                        .header(
                                "Authorization",
                                "Bearer " + issuedToken.accessToken()
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(registeredUser.id().toString()))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.fullName")
                        .value("Current User Test"))
                .andExpect(jsonPath("$.roles[0]")
                        .value("EMPLOYEE"));
    }

    private static String uniqueEmail() {
        return "current-user-" + UUID.randomUUID() + "@example.com";
    }
}