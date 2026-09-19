package com.smartdesk.api.identity.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

/**
 * Read-only representation of a service desk role.
 *
 * Role records are created and maintained through Flyway migrations.
 */
@Entity
@Immutable
@Table(name = "roles")
public class Role {

    @Id
    @Column(name = "id", nullable = false)
    private Short id;

    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, unique = true, length = 32)
    private RoleCode code;

    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private Instant createdAt;

    protected Role() {
        // Required by JPA.
    }

    public Short getId() {
        return id;
    }

    public RoleCode getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}