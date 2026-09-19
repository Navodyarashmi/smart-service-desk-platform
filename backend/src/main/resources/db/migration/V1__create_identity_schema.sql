CREATE TABLE roles
(
    id          SMALLINT PRIMARY KEY,
    code        VARCHAR(32)  NOT NULL,
    name        VARCHAR(64)  NOT NULL,
    description VARCHAR(255) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_roles_code UNIQUE (code),
    CONSTRAINT ck_roles_code_not_blank CHECK (btrim(code) <> ''),
    CONSTRAINT ck_roles_name_not_blank CHECK (btrim(name) <> '')
);

CREATE TABLE app_users
(
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email          VARCHAR(254) NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    full_name      VARCHAR(150) NOT NULL,
    enabled        BOOLEAN      NOT NULL DEFAULT TRUE,
    account_locked BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version        BIGINT       NOT NULL DEFAULT 0,

    CONSTRAINT uk_app_users_email UNIQUE (email),
    CONSTRAINT ck_app_users_email_not_blank CHECK (btrim(email) <> ''),
    CONSTRAINT ck_app_users_email_lowercase CHECK (email = lower(email)),
    CONSTRAINT ck_app_users_password_hash_not_blank CHECK (btrim(password_hash) <> ''),
    CONSTRAINT ck_app_users_full_name_not_blank CHECK (btrim(full_name) <> '')
);

CREATE TABLE user_roles
(
    user_id     UUID        NOT NULL,
    role_id     SMALLINT    NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by UUID,

    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES app_users (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id)
        REFERENCES roles (id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_user_roles_assigned_by
        FOREIGN KEY (assigned_by)
        REFERENCES app_users (id)
        ON DELETE SET NULL
);

CREATE INDEX ix_user_roles_role_id
    ON user_roles (role_id);

INSERT INTO roles (id, code, name, description)
VALUES
    (1, 'EMPLOYEE', 'Employee', 'Creates and tracks service desk tickets.'),
    (2, 'TECHNICIAN', 'Technician', 'Handles and resolves assigned service desk tickets.'),
    (3, 'ADMINISTRATOR', 'Administrator', 'Manages users, roles, configuration, and service desk operations.');