package com.smartdesk.api.identity.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a role assigned to a user account.
 */
@Entity
@Table(name = "user_roles")
public class UserRoleAssignment {

    @EmbeddedId
    private UserRoleId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @MapsId("roleId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private UserAccount assignedBy;

    protected UserRoleAssignment() {
        // Required by JPA.
    }

    private UserRoleAssignment(
            UserAccount user,
            Role role,
            UserAccount assignedBy
    ) {
        this.user = requirePersistedUser(user);
        this.role = requirePersistedRole(role);
        this.assignedBy = assignedBy;
        this.id = new UserRoleId(user.getId(), role.getId());
    }

    public static UserRoleAssignment assign(
            UserAccount user,
            Role role,
            UserAccount assignedBy
    ) {
        return new UserRoleAssignment(user, role, assignedBy);
    }

    public UserRoleId getId() {
        return id;
    }

    public UserAccount getUser() {
        return user;
    }

    public Role getRole() {
        return role;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public UserAccount getAssignedBy() {
        return assignedBy;
    }

    private static UserAccount requirePersistedUser(UserAccount user) {
        Objects.requireNonNull(user, "User must not be null.");

        if (user.getId() == null) {
            throw new IllegalArgumentException(
                    "User must be persisted before assigning a role."
            );
        }

        return user;
    }

    private static Role requirePersistedRole(Role role) {
        Objects.requireNonNull(role, "Role must not be null.");

        if (role.getId() == null) {
            throw new IllegalArgumentException(
                    "Role must be persisted before it can be assigned."
            );
        }

        return role;
    }
}