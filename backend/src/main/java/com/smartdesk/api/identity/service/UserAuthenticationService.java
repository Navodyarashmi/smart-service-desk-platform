package com.smartdesk.api.identity.service;

import com.smartdesk.api.identity.model.RoleCode;
import com.smartdesk.api.identity.model.UserAccount;
import com.smartdesk.api.identity.repository.UserAccountRepository;
import com.smartdesk.api.identity.repository.UserRoleAssignmentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Verifies user credentials and account availability.
 */
@Service
public class UserAuthenticationService {

    private final UserAccountRepository userAccountRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAuthenticationService(
            UserAccountRepository userAccountRepository,
            UserRoleAssignmentRepository userRoleAssignmentRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userAccountRepository = userAccountRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public AuthenticatedUser authenticate(LoginCommand command) {
        Objects.requireNonNull(command, "Login command must not be null.");

        String normalizedEmail = normalizeEmail(command.email());
        String rawPassword = requireText(command.password(), "Password");

        UserAccount user = userAccountRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        if (!user.isEnabled() || user.isAccountLocked()) {
            throw new AccountUnavailableException();
        }

        Set<RoleCode> roles = userRoleAssignmentRepository
                .findAllByUser_Id(user.getId())
                .stream()
                .map(assignment -> assignment.getRole().getCode())
                .collect(Collectors.toUnmodifiableSet());

        if (roles.isEmpty()) {
            throw new IllegalStateException(
                    "Authenticated user has no assigned roles."
            );
        }

        return new AuthenticatedUser(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                roles
        );
    }

    private static String normalizeEmail(String email) {
        return requireText(email, "Email")
                .toLowerCase(Locale.ROOT);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank."
            );
        }

        return value.trim();
    }
}