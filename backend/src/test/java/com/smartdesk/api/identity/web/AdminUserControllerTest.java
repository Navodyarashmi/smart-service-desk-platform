package com.smartdesk.api.identity.web;

import com.smartdesk.api.identity.model.Role;
import com.smartdesk.api.identity.model.RoleCode;
import com.smartdesk.api.identity.model.UserAccount;
import com.smartdesk.api.identity.model.UserRoleAssignment;
import com.smartdesk.api.identity.repository.RoleRepository;
import com.smartdesk.api.identity.repository.UserAccountRepository;
import com.smartdesk.api.identity.repository.UserRoleAssignmentRepository;
import com.smartdesk.api.identity.service.AuthenticatedUser;
import com.smartdesk.api.identity.service.LoginCommand;
import com.smartdesk.api.identity.service.RegisterUserCommand;
import com.smartdesk.api.identity.service.RegisteredUser;
import com.smartdesk.api.identity.service.UserAuthenticationService;
import com.smartdesk.api.identity.service.UserRegistrationService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class AdminUserControllerTest {

    private static final String PASSWORD = "StrongPassword123!";

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private UserRegistrationService registrationService;
    @Autowired
    private UserAuthenticationService authenticationService;
    @Autowired
    private JwtTokenService tokenService;
    @Autowired
    private UserAccountRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRoleAssignmentRepository assignmentRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void administratorCanListUsersAndAssignTechnicianRole() throws Exception {
        String adminToken = createAdministratorToken();
        RegisteredUser target = register("Role Target");

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/admin/users/{id}", target.id())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "TECHNICIAN",
                                  "enabled": true,
                                  "accountLocked": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("TECHNICIAN"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void employeeCannotAccessAdministration() throws Exception {
        RegisteredUser employee = register("Standard Employee");

        mockMvc.perform(get("/api/v1/admin/users")
                        .header(
                                "Authorization",
                                "Bearer " + tokenFor(employee.email())
                        ))
                .andExpect(status().isForbidden());
    }

    private String createAdministratorToken() {
        RegisteredUser registered = register("Test Administrator");
        UserAccount user = userRepository.findById(registered.id())
                .orElseThrow();
        Role role = roleRepository.findByCode(RoleCode.ADMINISTRATOR)
                .orElseThrow();
        assignmentRepository.saveAndFlush(
                UserRoleAssignment.assign(user, role, null)
        );
        return tokenFor(registered.email());
    }

    private RegisteredUser register(String name) {
        return registrationService.register(new RegisterUserCommand(
                UUID.randomUUID() + "@example.com",
                PASSWORD,
                name
        ));
    }

    private String tokenFor(String email) {
        AuthenticatedUser user = authenticationService.authenticate(
                new LoginCommand(email, PASSWORD)
        );
        return tokenService.issueAccessToken(user).accessToken();
    }
}
