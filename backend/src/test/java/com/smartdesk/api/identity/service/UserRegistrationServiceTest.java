package com.smartdesk.api.identity.service;

import com.smartdesk.api.identity.model.RoleCode;
import com.smartdesk.api.identity.model.UserAccount;
import com.smartdesk.api.identity.repository.UserAccountRepository;
import com.smartdesk.api.identity.repository.UserRoleAssignmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class UserRegistrationServiceTest {

    @Autowired
    private UserRegistrationService userRegistrationService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserRoleAssignmentRepository userRoleAssignmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldRegisterEmployeeWithSecurePassword() {
        String email = uniqueEmail();

        RegisteredUser registeredUser = userRegistrationService.register(
                new RegisterUserCommand(
                        "  " + email.toUpperCase() + "  ",
                        "StrongPassword123!",
                        "  Test Employee  "
                )
        );

        assertNotNull(registeredUser.id());
        assertEquals(email, registeredUser.email());
        assertEquals("Test Employee", registeredUser.fullName());
        assertEquals(RoleCode.EMPLOYEE, registeredUser.role());

        UserAccount savedUser = userAccountRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow();

        assertNotEquals("StrongPassword123!", savedUser.getPasswordHash());
        assertTrue(savedUser.getPasswordHash().startsWith("{bcrypt}"));
        assertTrue(passwordEncoder.matches(
                "StrongPassword123!",
                savedUser.getPasswordHash()
        ));
        assertTrue(savedUser.isEnabled());
        assertFalse(savedUser.isAccountLocked());

        assertTrue(userRoleAssignmentRepository
                .existsByUser_IdAndRole_Code(
                        savedUser.getId(),
                        RoleCode.EMPLOYEE
                ));
    }

    @Test
    void shouldRejectAnAlreadyRegisteredEmail() {
        String email = uniqueEmail();

        userRegistrationService.register(
                new RegisterUserCommand(
                        email,
                        "StrongPassword123!",
                        "First Employee"
                )
        );

        assertThrows(
                EmailAlreadyRegisteredException.class,
                () -> userRegistrationService.register(
                        new RegisterUserCommand(
                                email.toUpperCase(),
                                "AnotherPassword123!",
                                "Second Employee"
                        )
                )
        );
    }

    private static String uniqueEmail() {
        return "employee-" + UUID.randomUUID() + "@example.com";
    }
}