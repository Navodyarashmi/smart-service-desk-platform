package com.smartdesk.api.identity.repository;

import com.smartdesk.api.identity.model.Role;
import com.smartdesk.api.identity.model.RoleCode;
import com.smartdesk.api.identity.model.UserAccount;
import com.smartdesk.api.identity.model.UserRoleAssignment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class UserRoleAssignmentRepositoryTest {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleAssignmentRepository assignmentRepository;

    @Test
    void shouldAssignEmployeeRoleToPersistedUser() {
        UserAccount user = UserAccount.register(
                "role.assignment@example.com",
                "secure-test-password-hash",
                "Role Assignment Test"
        );

        UserAccount savedUser = userAccountRepository.saveAndFlush(user);

        Role employeeRole = roleRepository
                .findByCode(RoleCode.EMPLOYEE)
                .orElseThrow();

        UserRoleAssignment assignment = UserRoleAssignment.assign(
                savedUser,
                employeeRole,
                null
        );

        UserRoleAssignment savedAssignment =
                assignmentRepository.saveAndFlush(assignment);

        assertNotNull(savedAssignment.getAssignedAt());
        assertEquals(
                savedUser.getId(),
                savedAssignment.getUser().getId()
        );
        assertEquals(
                RoleCode.EMPLOYEE,
                savedAssignment.getRole().getCode()
        );

        assertTrue(
                assignmentRepository.existsByUser_IdAndRole_Code(
                        savedUser.getId(),
                        RoleCode.EMPLOYEE
                )
        );

        List<UserRoleAssignment> assignments =
                assignmentRepository.findAllByUser_Id(savedUser.getId());

        assertEquals(1, assignments.size());
    }

    @Test
    void shouldRejectRoleAssignmentForUnpersistedUser() {
        UserAccount unpersistedUser = UserAccount.register(
                "not.persisted@example.com",
                "secure-test-password-hash",
                "Unpersisted User"
        );

        Role employeeRole = roleRepository
                .findByCode(RoleCode.EMPLOYEE)
                .orElseThrow();

        assertFalse(
                userAccountRepository.existsByEmailIgnoreCase(
                        unpersistedUser.getEmail()
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> UserRoleAssignment.assign(
                        unpersistedUser,
                        employeeRole,
                        null
                )
        );
    }
}