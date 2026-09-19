package com.smartdesk.api.identity.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

/**
 * Represents a user account that can access the service desk.
 */
@Entity
@Table(name = "app_users")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "account_locked", nullable = false)
    private boolean accountLocked;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected UserAccount() {
        // Required by JPA.
    }

    private UserAccount(
            String email,
            String passwordHash,
            String fullName
    ) {
        this.email = normalizeEmail(email);
        this.passwordHash = requireText(passwordHash, "Password hash");
        this.fullName = requireText(fullName, "Full name");
        this.enabled = true;
        this.accountLocked = false;
    }

    public static UserAccount register(
            String email,
            String passwordHash,
            String fullName
    ) {
        return new UserAccount(email, passwordHash, fullName);
    }

    public void updateFullName(String fullName) {
        this.fullName = requireText(fullName, "Full name");
    }

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = requireText(passwordHash, "Password hash");
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    public void lock() {
        this.accountLocked = true;
    }

    public void unlock() {
        this.accountLocked = false;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }

    private static String normalizeEmail(String email) {
        return requireText(email, "Email")
                .toLowerCase(Locale.ROOT);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }

        return value.trim();
    }
}