package com.smartdesk.api.identity.repository;

import com.smartdesk.api.identity.model.UserAccount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class UserAccountRepositoryTest {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Test
    void shouldSaveAndFindUserByEmailIgnoringCase() {
        UserAccount user = UserAccount.register(
                "Employee.One@Example.com",
                "secure-test-password-hash",
                "Employee One"
        );

        UserAccount savedUser = userAccountRepository.saveAndFlush(user);

        assertNotNull(savedUser.getId());
        assertEquals("employee.one@example.com", savedUser.getEmail());
        assertTrue(savedUser.isEnabled());
        assertFalse(savedUser.isAccountLocked());
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getUpdatedAt());

        assertTrue(
                userAccountRepository.existsByEmailIgnoreCase(
                        "EMPLOYEE.ONE@EXAMPLE.COM"
                )
        );

        UserAccount foundUser = userAccountRepository
                .findByEmailIgnoreCase("EMPLOYEE.ONE@EXAMPLE.COM")
                .orElseThrow();

        assertEquals(savedUser.getId(), foundUser.getId());
        assertEquals("Employee One", foundUser.getFullName());
    }

    @Test
    void shouldRejectBlankRegistrationFields() {
        assertThrows(
                IllegalArgumentException.class,
                () -> UserAccount.register(
                        " ",
                        "secure-test-password-hash",
                        "Employee One"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> UserAccount.register(
                        "employee@example.com",
                        " ",
                        "Employee One"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> UserAccount.register(
                        "employee@example.com",
                        "secure-test-password-hash",
                        " "
                )
        );
    }
}