package com.smartdesk.api.identity.service;

import com.smartdesk.api.identity.model.Role;
import com.smartdesk.api.identity.model.RoleCode;
import com.smartdesk.api.identity.model.UserAccount;
import com.smartdesk.api.identity.model.UserRoleAssignment;
import com.smartdesk.api.identity.repository.RoleRepository;
import com.smartdesk.api.identity.repository.UserAccountRepository;
import com.smartdesk.api.identity.repository.UserRoleAssignmentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;

/**
 * Handles registration of employee accounts.
 */
@Service
public class UserRegistrationService {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationService(
            UserAccountRepository userAccountRepository,
            RoleRepository roleRepository,
            UserRoleAssignmentRepository userRoleAssignmentRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new enabled account with the default EMPLOYEE role.
     */
    @Transactional
    public RegisteredUser register(RegisterUserCommand command) {
        Objects.requireNonNull(command, "Registration command must not be null.");

        String normalizedEmail = normalizeEmail(command.email());
        String rawPassword = requireText(command.password(), "Password");
        String fullName = requireText(command.fullName(), "Full name");

        if (userAccountRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new EmailAlreadyRegisteredException();
        }

        Role employeeRole = roleRepository.findByCode(RoleCode.EMPLOYEE)
                .orElseThrow(() -> new IllegalStateException(
                        "Required EMPLOYEE role is not configured."
                ));

        String passwordHash = passwordEncoder.encode(rawPassword);

        UserAccount user = UserAccount.register(
                normalizedEmail,
                passwordHash,
                fullName
        );

        UserAccount savedUser = userAccountRepository.save(user);

        UserRoleAssignment roleAssignment = UserRoleAssignment.assign(
                savedUser,
                employeeRole,
                null
        );

        userRoleAssignmentRepository.save(roleAssignment);

        return new RegisteredUser(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                employeeRole.getCode()
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