package com.smartdesk.api.ticket.model;

import com.smartdesk.api.identity.model.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a support request submitted to the service desk.
 */
@Entity
@Table(name = "service_tickets")
public class ServiceTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(
            name = "reference_code",
            nullable = false,
            unique = true,
            length = 20
    )
    private String referenceCode;

    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "description", nullable = false, length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    private TicketCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 16)
    private TicketPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    private TicketStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private UserAccount requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private UserAccount assignee;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected ServiceTicket() {
        // Required by JPA.
    }

    private ServiceTicket(
            String referenceCode,
            String title,
            String description,
            TicketCategory category,
            TicketPriority priority,
            UserAccount requester
    ) {
        this.referenceCode = requireText(
                referenceCode,
                "Reference code"
        );
        this.title = requireText(title, "Title");
        this.description = requireText(description, "Description");
        this.category = Objects.requireNonNull(
                category,
                "Category must not be null."
        );
        this.priority = Objects.requireNonNull(
                priority,
                "Priority must not be null."
        );
        this.requester = Objects.requireNonNull(
                requester,
                "Requester must not be null."
        );
        this.status = TicketStatus.OPEN;
    }

    public static ServiceTicket open(
            String referenceCode,
            String title,
            String description,
            TicketCategory category,
            TicketPriority priority,
            UserAccount requester
    ) {
        return new ServiceTicket(
                referenceCode,
                title,
                description,
                category,
                priority,
                requester
        );
    }

    public void updateDetails(
            String title,
            String description,
            TicketCategory category,
            TicketPriority priority
    ) {
        this.title = requireText(title, "Title");
        this.description = requireText(description, "Description");
        this.category = Objects.requireNonNull(
                category,
                "Category must not be null."
        );
        this.priority = Objects.requireNonNull(
                priority,
                "Priority must not be null."
        );
    }
    public void assignTo(UserAccount technician) {
        this.assignee = Objects.requireNonNull(
                technician,
                "Technician must not be null."
        );
        this.status = TicketStatus.ASSIGNED;
    }

    public void beginProgress() {
        this.status = TicketStatus.IN_PROGRESS;
    }

    public void resolve() {
        this.status = TicketStatus.RESOLVED;
        this.resolvedAt = Instant.now();
    }

    public void close() {
        this.status = TicketStatus.CLOSED;
    }

    public void cancel() {
        this.status = TicketStatus.CANCELLED;
    }

    public UUID getId() {
        return id;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TicketCategory getCategory() {
        return category;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public UserAccount getRequester() {
        return requester;
    }

    public UserAccount getAssignee() {
        return assignee;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public long getVersion() {
        return version;
    }

    private static String requireText(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank."
            );
        }

        return value.trim();
    }
}