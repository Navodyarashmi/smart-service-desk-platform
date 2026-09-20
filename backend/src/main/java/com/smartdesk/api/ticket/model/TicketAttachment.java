package com.smartdesk.api.ticket.model;

import com.smartdesk.api.identity.model.UserAccount;
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
@Table(name = "ticket_attachments")
public class TicketAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private ServiceTicket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id")
    private UserAccount uploader;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false, unique = true, length = 100)
    private String storedFilename;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected TicketAttachment() {
        // Required by JPA.
    }

    private TicketAttachment(
            ServiceTicket ticket,
            UserAccount uploader,
            String originalFilename,
            String storedFilename,
            String contentType,
            long sizeBytes
    ) {
        this.ticket = Objects.requireNonNull(ticket);
        this.uploader = Objects.requireNonNull(uploader);
        this.originalFilename = Objects.requireNonNull(originalFilename);
        this.storedFilename = Objects.requireNonNull(storedFilename);
        this.contentType = Objects.requireNonNull(contentType);
        this.sizeBytes = sizeBytes;
    }

    public static TicketAttachment create(
            ServiceTicket ticket,
            UserAccount uploader,
            String originalFilename,
            String storedFilename,
            String contentType,
            long sizeBytes
    ) {
        return new TicketAttachment(
                ticket,
                uploader,
                originalFilename,
                storedFilename,
                contentType,
                sizeBytes
        );
    }

    public UUID getId() { return id; }
    public ServiceTicket getTicket() { return ticket; }
    public UserAccount getUploader() { return uploader; }
    public String getOriginalFilename() { return originalFilename; }
    public String getStoredFilename() { return storedFilename; }
    public String getContentType() { return contentType; }
    public long getSizeBytes() { return sizeBytes; }
    public Instant getCreatedAt() { return createdAt; }
}
