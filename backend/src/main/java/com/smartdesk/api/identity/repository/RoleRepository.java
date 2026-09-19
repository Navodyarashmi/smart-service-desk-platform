package com.smartdesk.api.identity.repository;

import com.smartdesk.api.identity.model.Role;
import com.smartdesk.api.identity.model.RoleCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Provides database access for service desk roles.
 */
public interface RoleRepository extends JpaRepository<Role, Short> {

    Optional<Role> findByCode(RoleCode code);
}