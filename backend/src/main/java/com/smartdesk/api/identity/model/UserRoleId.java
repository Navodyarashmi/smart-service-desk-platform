package com.smartdesk.api.identity.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite primary key for a user-role assignment.
 */
@Embeddable
public class UserRoleId implements Serializable {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "role_id", nullable = false)
    private Short roleId;

    protected UserRoleId() {
        // Required by JPA.
    }

    public UserRoleId(UUID userId, Short roleId) {
        this.userId = Objects.requireNonNull(
                userId,
                "User ID must not be null."
        );
        this.roleId = Objects.requireNonNull(
                roleId,
                "Role ID must not be null."
        );
    }

    public UUID getUserId() {
        return userId;
    }

    public Short getRoleId() {
        return roleId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof UserRoleId that)) {
            return false;
        }

        return Objects.equals(userId, that.userId)
                && Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roleId);
    }
}