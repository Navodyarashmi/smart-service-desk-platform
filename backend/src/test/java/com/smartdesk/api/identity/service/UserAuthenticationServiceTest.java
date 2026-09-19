package com.smartdesk.api.identity.service;

import com.smartdesk.api.identity.model.RoleCode;
import com.smartdesk.api.identity.model.UserAccount;
import com.smartdesk.api.identity.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class UserAuthenticationServiceTest {

    private static final String VALID_PASSWORD = "StrongPassword123!";

    @Autowired
    private UserAuthenticationService userAuthenticationService;

    @Autowired
    private UserRegistrationService userRegistrationService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Test
    void shouldAuthenticateEnabledEmployee() {
        String email = uniqueEmail();
        RegisteredUser registeredUser = registerUser(email);

        AuthenticatedUser authenticatedUser =
                userAuthenticationService.authenticate(
                        new LoginCommand(
                                email.toUpperCase(),
                                VALID_PASSWORD
                        )
                );

        assertEquals(registeredUser.id(), authenticatedUser.id());
        assertEquals(email, authenticatedUser.email());
        assertEquals("Authentication Test User", authenticatedUser.fullName());
        assertEquals(
                Set.of(RoleCode.EMPLOYEE),
                authenticatedUser.roles()
        );
    }

    @Test
    void shouldRejectIncorrectPassword() {
        String email = uniqueEmail();
        registerUser(email);

        assertThrows(
                InvalidCredentialsException.class,
                () -> userAuthenticationService.authenticate(
                        new LoginCommand(
                                email,
                                "IncorrectPassword123!"
                        )
                )
        );
    }

    @Test
    void shouldRejectUnknownEmail() {
        assertThrows(
                InvalidCredentialsException.class,
                () -> userAuthenticationService.authenticate(
                        new LoginCommand(
                                uniqueEmail(),
                                VALID_PASSWORD
                        )
                )
        );
    }

    @Test
    void shouldRejectDisabledAccount() {
        String email = uniqueEmail();
        RegisteredUser registeredUser = registerUser(email);

        UserAccount user = userAccountRepository
                .findById(registeredUser.id())
                .orElseThrow();

        user.disable();
        userAccountRepository.save(user);

        assertThrows(
                AccountUnavailableException.class,
                () -> userAuthenticationService.authenticate(
                        new LoginCommand(email, VALID_PASSWORD)
                )
        );
    }

    @Test
    void shouldRejectLockedAccount() {
        String email = uniqueEmail();
        RegisteredUser registeredUser = registerUser(email);

        UserAccount user = userAccountRepository
                .findById(registeredUser.id())
                .orElseThrow();

        user.lock();
        userAccountRepository.save(user);

        assertThrows(
                AccountUnavailableException.class,
                () -> userAuthenticationService.authenticate(
                        new LoginCommand(email, VALID_PASSWORD)
                )
        );
    }

    private RegisteredUser registerUser(String email) {
        return userRegistrationService.register(
                new RegisterUserCommand(
                        email,
                        VALID_PASSWORD,
                        "Authentication Test User"
                )
        );
    }

    private static String uniqueEmail() {
        return "authentication-" + UUID.randomUUID() + "@example.com";
    }
}