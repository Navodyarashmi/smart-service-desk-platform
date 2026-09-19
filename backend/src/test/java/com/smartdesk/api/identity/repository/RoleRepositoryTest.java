package com.smartdesk.api.identity.repository;

import com.smartdesk.api.identity.model.Role;
import com.smartdesk.api.identity.model.RoleCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void shouldContainThreeSeededRoles() {
        assertEquals(3, roleRepository.count());
    }

    @Test
    void shouldFindEmployeeRoleByCode() {
        Role employeeRole = roleRepository
                .findByCode(RoleCode.EMPLOYEE)
                .orElseThrow();

        assertEquals(Short.valueOf((short) 1), employeeRole.getId());
        assertEquals("Employee", employeeRole.getName());
        assertTrue(employeeRole.getDescription().contains("tickets"));
    }
}