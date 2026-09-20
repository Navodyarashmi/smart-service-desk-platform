package com.smartdesk.api.ticket.web;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.attachments.directory=target/test-attachments")
@Transactional
class TicketAttachmentControllerTest {

    private static final String PASSWORD = "StrongPassword123!";
    private static final Path TEST_STORAGE = Path.of("target/test-attachments");

    @Autowired private WebApplicationContext context;
    @Autowired private UserRegistrationService registrationService;
    @Autowired private UserAuthenticationService authenticationService;
    @Autowired private JwtTokenService tokenService;
    @Autowired private TicketCreationService ticketCreationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @AfterEach
    void cleanStoredFiles() throws IOException {
        if (!Files.exists(TEST_STORAGE)) return;
        try (var paths = Files.walk(TEST_STORAGE)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException exception) {
                    throw new IllegalStateException(exception);
                }
            });
        }
    }

    @Test
    void requesterCanUploadListAndDownloadSafeAttachment() throws Exception {
        RegisteredUser requester = register("Attachment Requester");
        CreatedTicket ticket = createTicket(requester.id());
        String token = tokenFor(requester.email());
        byte[] pdfContent = "%PDF-1.4 test attachment".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "diagnostic-report.pdf",
                "application/pdf",
                pdfContent
        );

        String attachmentId = mockMvc.perform(multipart(
                                "/api/v1/tickets/{id}/attachments",
                                ticket.id()
                        )
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").value("diagnostic-report.pdf"))
                .andExpect(jsonPath("$.contentType").value("application/pdf"))
                .andReturn().getResponse().getContentAsString()
                .replaceFirst(".*\\\"id\\\":\\\"([^\\\"]+)\\\".*", "$1");

        mockMvc.perform(get("/api/v1/tickets/{id}/attachments", ticket.id())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].filename").value("diagnostic-report.pdf"));

        mockMvc.perform(get(
                        "/api/v1/tickets/{ticketId}/attachments/{attachmentId}/content",
                        ticket.id(),
                        UUID.fromString(attachmentId)
                ).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().bytes(pdfContent));
    }

    @Test
    void unrelatedEmployeeCannotReadTicketAttachments() throws Exception {
        RegisteredUser owner = register("Attachment Owner");
        RegisteredUser unrelated = register("Unrelated Employee");
        CreatedTicket ticket = createTicket(owner.id());

        mockMvc.perform(get("/api/v1/tickets/{id}/attachments", ticket.id())
                        .header(
                                "Authorization",
                                "Bearer " + tokenFor(unrelated.email())
                        ))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadRejectsContentThatDoesNotMatchDeclaredType() throws Exception {
        RegisteredUser requester = register("Validated Requester");
        CreatedTicket ticket = createTicket(requester.id());
        MockMultipartFile disguisedFile = new MockMultipartFile(
                "file",
                "unsafe.pdf",
                "application/pdf",
                "This is not a PDF file".getBytes()
        );

        mockMvc.perform(multipart(
                                "/api/v1/tickets/{id}/attachments",
                                ticket.id()
                        )
                        .file(disguisedFile)
                        .header(
                                "Authorization",
                                "Bearer " + tokenFor(requester.email())
                        ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "The file content does not match its declared type."
                ));
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
                "Attachment test ticket",
                "A sufficiently detailed attachment test description.",
                TicketCategory.SOFTWARE,
                TicketPriority.MEDIUM
        ));
    }

    private String tokenFor(String email) {
        AuthenticatedUser user = authenticationService.authenticate(
                new LoginCommand(email, PASSWORD)
        );
        return tokenService.issueAccessToken(user).accessToken();
    }
}
