package com.smartdesk.api.identity.model;

import com.smartdesk.api.ticket.model.ServiceTicket;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "notifications")
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "recipient_id")
    private UserAccount recipient;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ticket_id")
    private ServiceTicket ticket;
    @Column(name = "message", nullable = false, length = 500)
    private String message;
    @Column(name = "read_at")
    private Instant readAt;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Notification() { }
    private Notification(UserAccount recipient, ServiceTicket ticket, String message) {
        this.recipient = Objects.requireNonNull(recipient);
        this.ticket = ticket;
        this.message = message.trim();
    }
    public static Notification create(UserAccount recipient, ServiceTicket ticket, String message) { return new Notification(recipient, ticket, message); }
    public void markRead() { this.readAt = Instant.now(); }
    public UUID getId() { return id; }
    public ServiceTicket getTicket() { return ticket; }
    public String getMessage() { return message; }
    public Instant getReadAt() { return readAt; }
    public Instant getCreatedAt() { return createdAt; }
}
