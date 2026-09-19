package com.smartdesk.api.identity.web;

import com.smartdesk.api.identity.model.UserAccount;
import com.smartdesk.api.identity.repository.UserAccountRepository;
import com.smartdesk.api.identity.service.RegisterUserCommand;
import com.smartdesk.api.identity.service.RegisteredUser;
import com.smartdesk.api.identity.service.UserRegistrationService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class LoginControllerTest {

    private static final String VALID_PASSWORD = "StrongPassword123!";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRegistrationService userRegistrationService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    @Test
    void shouldLoginAndReturnAccessToken() throws Exception {
        String email = uniqueEmail();
        RegisteredUser registeredUser = registerUser(email);

        String requestBody = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email.toUpperCase(), VALID_PASSWORD);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresAt").isNotEmpty())
                .andExpect(jsonPath("$.userId")
                        .value(registeredUser.id().toString()))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.fullName")
                        .value("Login Test User"))
                .andExpect(jsonPath("$.roles[0]").value("EMPLOYEE"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void shouldRejectIncorrectCredentials() throws Exception {
        String email = uniqueEmail();
        registerUser(email);

        String requestBody = """
                {
                  "email": "%s",
                  "password": "IncorrectPassword123!"
                }
                """.formatted(email);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message")
                        .value("Email or password is incorrect."));
    }

    @Test
    void shouldRejectInvalidLoginInput() throws Exception {
        String requestBody = """
                {
                  "email": "invalid-email",
                  "password": "short"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Request validation failed."))
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void shouldRejectDisabledAccount() throws Exception {
        String email = uniqueEmail();
        RegisteredUser registeredUser = registerUser(email);

        UserAccount user = userAccountRepository
                .findById(registeredUser.id())
                .orElseThrow();

        user.disable();
        userAccountRepository.save(user);

        String requestBody = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, VALID_PASSWORD);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message")
                        .value("This account is currently unavailable."));
    }

    private RegisteredUser registerUser(String email) {
        return userRegistrationService.register(
                new RegisterUserCommand(
                        email,
                        VALID_PASSWORD,
                        "Login Test User"
                )
        );
    }

    private static String uniqueEmail() {
        return "login-" + UUID.randomUUID() + "@example.com";
    }
}