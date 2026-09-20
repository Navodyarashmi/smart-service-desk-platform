CREATE TABLE ticket_activity
(
    id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id     UUID          NOT NULL,
    actor_id      UUID,
    activity_type VARCHAR(32)   NOT NULL,
    message       VARCHAR(2000) NOT NULL,
    internal_note BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ticket_activity_ticket FOREIGN KEY (ticket_id)
        REFERENCES service_tickets (id) ON DELETE CASCADE,
    CONSTRAINT fk_ticket_activity_actor FOREIGN KEY (actor_id)
        REFERENCES app_users (id) ON DELETE SET NULL,
    CONSTRAINT ck_ticket_activity_type CHECK (
        activity_type IN ('CREATED', 'COMMENT', 'INTERNAL_NOTE', 'ASSIGNED', 'STATUS_CHANGED', 'UPDATED', 'CANCELLED')
    ),
    CONSTRAINT ck_ticket_activity_message CHECK (btrim(message) <> '')
);

CREATE INDEX ix_ticket_activity_ticket_created
    ON ticket_activity (ticket_id, created_at);

CREATE TABLE notifications
(
    id           UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id UUID          NOT NULL,
    ticket_id    UUID,
    message      VARCHAR(500)  NOT NULL,
    read_at      TIMESTAMPTZ,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id)
        REFERENCES app_users (id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_ticket FOREIGN KEY (ticket_id)
        REFERENCES service_tickets (id) ON DELETE CASCADE,
    CONSTRAINT ck_notifications_message CHECK (btrim(message) <> '')
);

CREATE INDEX ix_notifications_recipient_created
    ON notifications (recipient_id, created_at DESC);
