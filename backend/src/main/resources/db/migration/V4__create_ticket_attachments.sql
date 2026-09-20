ALTER TABLE ticket_activity
    DROP CONSTRAINT ck_ticket_activity_type;

ALTER TABLE ticket_activity
    ADD CONSTRAINT ck_ticket_activity_type CHECK (
        activity_type IN (
            'CREATED', 'COMMENT', 'INTERNAL_NOTE', 'ASSIGNED',
            'STATUS_CHANGED', 'UPDATED', 'CANCELLED', 'ATTACHMENT_ADDED'
        )
    );

CREATE TABLE ticket_attachments
(
    id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id         UUID         NOT NULL,
    uploader_id       UUID,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename   VARCHAR(100) NOT NULL,
    content_type      VARCHAR(100) NOT NULL,
    size_bytes        BIGINT       NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_ticket_attachments_stored_filename UNIQUE (stored_filename),
    CONSTRAINT fk_ticket_attachments_ticket FOREIGN KEY (ticket_id)
        REFERENCES service_tickets (id) ON DELETE CASCADE,
    CONSTRAINT fk_ticket_attachments_uploader FOREIGN KEY (uploader_id)
        REFERENCES app_users (id) ON DELETE SET NULL,
    CONSTRAINT ck_ticket_attachments_name CHECK (btrim(original_filename) <> ''),
    CONSTRAINT ck_ticket_attachments_size CHECK (size_bytes BETWEEN 1 AND 5242880)
);

CREATE INDEX ix_ticket_attachments_ticket_created
    ON ticket_attachments (ticket_id, created_at);
