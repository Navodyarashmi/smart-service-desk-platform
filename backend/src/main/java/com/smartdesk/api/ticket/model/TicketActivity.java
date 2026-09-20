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
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "ticket_activity")
public class TicketActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private ServiceTicket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private UserAccount actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 32)
    private TicketActivityType activityType;

    @Column(name = "message", nullable = false, length = 2000)
    private String message;

    @Column(name = "internal_note", nullable = false)
    private boolean internalNote;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected TicketActivity() {
    }

    private TicketActivity(ServiceTicket ticket, UserAccount actor, TicketActivityType type, String message, boolean internalNote) {
        this.ticket = Objects.requireNonNull(ticket);
        this.actor = actor;
        this.activityType = Objects.requireNonNull(type);
        if (message == null || message.isBlank()) throw new IllegalArgumentException("Activity message must not be blank.");
        this.message = message.trim();
        this.internalNote = internalNote;
    }

    public static TicketActivity record(ServiceTicket ticket, UserAccount actor, TicketActivityType type, String message, boolean internalNote) {
        return new TicketActivity(ticket, actor, type, message, internalNote);
    }

    public UUID getId() { return id; }
    public UserAccount getActor() { return actor; }
    public TicketActivityType getActivityType() { return activityType; }
    public String getMessage() { return message; }
    public boolean isInternalNote() { return internalNote; }
    public Instant getCreatedAt() { return createdAt; }
}
