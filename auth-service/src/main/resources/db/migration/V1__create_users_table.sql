-- V1__create_users_table.sql
--
-- Flyway naming rule: V{number}__{description}.sql
-- The double underscore is required.
-- Files run in version order — V1 before V2 before V3.
-- Once a file runs it must NEVER be modified.
-- Add a new file V2__... to make further changes.

CREATE TABLE IF NOT EXISTS users
(
    id                   VARCHAR(36) NOT NULL,
    email                VARCHAR(255) NOT NULL,
    password_hash        VARCHAR(255) NOT NULL,
    phone_number         VARCHAR(20)  NOT NULL,
    transaction_pin_hash VARCHAR(255),

    -- PENDING_VERIFICATION | ACTIVE | LOCKED | SUSPENDED
    status               VARCHAR(30)  NOT NULL DEFAULT 'PENDING_VERIFICATION',
    failed_login_attempts INTEGER     NOT NULL DEFAULT 0,
    locked_until         TIMESTAMPTZ,
    last_login_at        TIMESTAMPTZ,

    -- TIMESTAMPTZ stores moment in UTC regardless of server timezone.
    -- Always use TIMESTAMPTZ for audit fields, never TIMESTAMP.
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_users       PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email)
);

-- Roles stored in a join table.
-- One user can have multiple roles.
-- CASCADE DELETE means when a user is deleted,
-- their roles are automatically deleted too.
CREATE TABLE IF NOT EXISTS user_roles
(
    user_id VARCHAR(36) NOT NULL,
    role    VARCHAR(50) NOT NULL,

    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user
    FOREIGN KEY (user_id)
    REFERENCES users (id)
    ON DELETE CASCADE
    );

-- Index on email because every login query searches by email.
-- Without this index, PostgreSQL scans the entire users table
-- on every login attempt — catastrophic at scale.
CREATE INDEX IF NOT EXISTS idx_users_email
    ON users (email);

CREATE INDEX IF NOT EXISTS idx_users_status
    ON users (status);

-- Trigger: automatically update updated_at on every row change.
-- Defense in depth — even if application code forgets to set it,
-- the database enforces it.
CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();


