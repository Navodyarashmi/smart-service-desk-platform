package com.smartdesk.api.identity.repository;

import com.smartdesk.api.identity.model.RoleCode;
import com.smartdesk.api.identity.model.UserRoleAssignment;
import com.smartdesk.api.identity.model.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Provides database access for user-role assignments.
 */
public interface UserRoleAssignmentRepository
        extends JpaRepository<UserRoleAssignment, UserRoleId> {

    List<UserRoleAssignment> findAllByUser_Id(UUID userId);

    boolean existsByUser_IdAndRole_Code(
            UUID userId,
            RoleCode roleCode
    );

    void deleteAllByUser_Id(UUID userId);
}
