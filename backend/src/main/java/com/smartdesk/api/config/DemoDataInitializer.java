package com.smartdesk.api.config;

import com.smartdesk.api.identity.model.Role;
import com.smartdesk.api.identity.model.RoleCode;
import com.smartdesk.api.identity.model.UserAccount;
import com.smartdesk.api.identity.model.UserRoleAssignment;
import com.smartdesk.api.identity.repository.RoleRepository;
import com.smartdesk.api.identity.repository.UserAccountRepository;
import com.smartdesk.api.identity.repository.UserRoleAssignmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates non-production portfolio accounts when explicitly enabled.
 */
@Component
public class DemoDataInitializer implements ApplicationRunner {

    private final UserAccountRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleAssignmentRepository assignmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;
    private final String password;

    public DemoDataInitializer(
            UserAccountRepository userRepository,
            RoleRepository roleRepository,
            UserRoleAssignmentRepository assignmentRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.demo.seed-enabled:false}") boolean enabled,
            @Value("${app.demo.password:}") String password
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.assignmentRepository = assignmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }

        if (password == null || password.length() < 12) {
            throw new IllegalStateException(
                    "DEMO_PASSWORD must contain at least 12 characters "
                            + "when demo seeding is enabled."
            );
        }

        createAccount(
                "employee@helphub.demo",
                "Maya Employee",
                RoleCode.EMPLOYEE
        );
        createAccount(
                "technician@helphub.demo",
                "Theo Technician",
                RoleCode.TECHNICIAN
        );
        createAccount(
                "admin@helphub.demo",
                "Avery Administrator",
                RoleCode.ADMINISTRATOR
        );
    }

    private void createAccount(
            String email,
            String fullName,
            RoleCode roleCode
    ) {
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IllegalStateException(
                        "Required role is missing: " + roleCode
                ));
        UserAccount user = userRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> userRepository.saveAndFlush(
                        UserAccount.register(
                                email,
                                passwordEncoder.encode(password),
                                fullName
                        )
                ));

        if (!assignmentRepository.existsByUser_IdAndRole_Code(
                user.getId(),
                roleCode
        )) {
            assignmentRepository.save(
                    UserRoleAssignment.assign(user, role, null)
            );
        }
    }
}
