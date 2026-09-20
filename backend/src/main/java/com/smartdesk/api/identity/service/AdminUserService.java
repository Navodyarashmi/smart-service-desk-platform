package com.smartdesk.api.identity.service;

import com.smartdesk.api.identity.model.Role;
import com.smartdesk.api.identity.model.RoleCode;
import com.smartdesk.api.identity.model.UserAccount;
import com.smartdesk.api.identity.model.UserRoleAssignment;
import com.smartdesk.api.identity.repository.RoleRepository;
import com.smartdesk.api.identity.repository.UserAccountRepository;
import com.smartdesk.api.identity.repository.UserRoleAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

    private final UserAccountRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleAssignmentRepository assignmentRepository;

    public AdminUserService(
            UserAccountRepository userRepository,
            RoleRepository roleRepository,
            UserRoleAssignmentRepository assignmentRepository
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminUserSummary> listUsers() {
        return userRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(UserAccount::getCreatedAt).reversed())
                .map(this::toSummary)
                .toList();
    }

    @Transactional
    public AdminUserSummary updateUser(
            UUID actorId,
            UUID userId,
            RoleCode roleCode,
            boolean enabled,
            boolean accountLocked
    ) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User account was not found."
                ));

        if (actorId.equals(userId)
                && (roleCode != RoleCode.ADMINISTRATOR
                || !enabled
                || accountLocked)) {
            throw new AdminActionConflictException(
                    "Administrators cannot remove or lock their own access."
            );
        }

        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IllegalStateException(
                        "Required role is missing: " + roleCode
                ));

        if (enabled) {
            user.enable();
        } else {
            user.disable();
        }

        if (accountLocked) {
            user.lock();
        } else {
            user.unlock();
        }

        assignmentRepository.deleteAllByUser_Id(userId);
        assignmentRepository.flush();
        assignmentRepository.save(
                UserRoleAssignment.assign(user, role, userRepository.getReferenceById(actorId))
        );
        userRepository.saveAndFlush(user);
        return toSummary(user);
    }

    private AdminUserSummary toSummary(UserAccount user) {
        Set<RoleCode> roles = assignmentRepository
                .findAllByUser_Id(user.getId())
                .stream()
                .map(assignment -> assignment.getRole().getCode())
                .collect(Collectors.toUnmodifiableSet());

        return new AdminUserSummary(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                roles,
                user.isEnabled(),
                user.isAccountLocked(),
                user.getCreatedAt()
        );
    }
}
