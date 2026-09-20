CREATE TABLE service_tickets
(
    id             UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    reference_code VARCHAR(20)   NOT NULL,
    title          VARCHAR(160)  NOT NULL,
    description    VARCHAR(4000) NOT NULL,
    category       VARCHAR(32)   NOT NULL,
    priority       VARCHAR(16)   NOT NULL,
    status         VARCHAR(24)   NOT NULL DEFAULT 'OPEN',
    requester_id   UUID          NOT NULL,
    assignee_id    UUID,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at    TIMESTAMPTZ,
    version        BIGINT        NOT NULL DEFAULT 0,

    CONSTRAINT uk_service_tickets_reference_code
        UNIQUE (reference_code),

    CONSTRAINT ck_service_tickets_reference_not_blank
        CHECK (btrim(reference_code) <> ''),

    CONSTRAINT ck_service_tickets_title_not_blank
        CHECK (btrim(title) <> ''),

    CONSTRAINT ck_service_tickets_description_not_blank
        CHECK (btrim(description) <> ''),

    CONSTRAINT ck_service_tickets_category
        CHECK (
            category IN (
                'HARDWARE',
                'SOFTWARE',
                'NETWORK',
                'ACCESS',
                'OTHER'
            )
        ),

    CONSTRAINT ck_service_tickets_priority
        CHECK (
            priority IN (
                'LOW',
                'MEDIUM',
                'HIGH',
                'URGENT'
            )
        ),

    CONSTRAINT ck_service_tickets_status
        CHECK (
            status IN (
                'OPEN',
                'ASSIGNED',
                'IN_PROGRESS',
                'RESOLVED',
                'CLOSED',
                'CANCELLED'
            )
        ),

    CONSTRAINT fk_service_tickets_requester
        FOREIGN KEY (requester_id)
        REFERENCES app_users (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_service_tickets_assignee
        FOREIGN KEY (assignee_id)
        REFERENCES app_users (id)
        ON DELETE SET NULL
);

CREATE INDEX ix_service_tickets_requester_created
    ON service_tickets (requester_id, created_at DESC);

CREATE INDEX ix_service_tickets_assignee_status
    ON service_tickets (assignee_id, status);

CREATE INDEX ix_service_tickets_status_priority
    ON service_tickets (status, priority);