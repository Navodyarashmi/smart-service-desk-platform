package com.smartdesk.api.ticket.web;

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
class StaffTicketControllerTest {

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
    private TicketCreationService ticketCreationService;
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
    void technicianCanClaimAndProgressTicket() throws Exception {
        RegisteredUser requester = register("Queue Requester");
        CreatedTicket ticket = createTicket(requester.id());
        String technicianToken = createTechnicianToken();

        mockMvc.perform(get("/api/v1/staff/tickets")
                        .header("Authorization", "Bearer " + technicianToken))
                .andExpect(status().isOk());

        mockMvc.perform(post(
                        "/api/v1/staff/tickets/{id}/claim",
                        ticket.id()
                ).header("Authorization", "Bearer " + technicianToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"));

        mockMvc.perform(patch(
                        "/api/v1/staff/tickets/{id}/status",
                        ticket.id()
                ).header("Authorization", "Bearer " + technicianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void employeeCannotOpenStaffQueue() throws Exception {
        String employeeToken = tokenFor(register("Regular Employee").email());

        mockMvc.perform(get("/api/v1/staff/tickets")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    private RegisteredUser register(String name) {
        return registrationService.register(new RegisterUserCommand(
                UUID.randomUUID() + "@example.com",
                PASSWORD,
                name
        ));
    }

    private CreatedTicket createTicket(UUID requesterId) {
        return ticketCreationService.create(new CreateTicketCommand(
                requesterId,
                "Unable to access shared drive",
                "The shared drive reports an access denied message.",
                TicketCategory.ACCESS,
                TicketPriority.HIGH
        ));
    }

    private String createTechnicianToken() {
        RegisteredUser registered = register("Service Technician");
        UserAccount user = userRepository.findById(registered.id())
                .orElseThrow();
        Role role = roleRepository.findByCode(RoleCode.TECHNICIAN)
                .orElseThrow();
        assignmentRepository.saveAndFlush(
                UserRoleAssignment.assign(user, role, null)
        );
        return tokenFor(registered.email());
    }

    private String tokenFor(String email) {
        AuthenticatedUser user = authenticationService.authenticate(
                new LoginCommand(email, PASSWORD)
        );
        return tokenService.issueAccessToken(user).accessToken();
    }
}
