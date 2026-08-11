CREATE TABLE IF NOT EXISTS notifications
(
    id             VARCHAR(36)  NOT NULL,
    recipient_id   VARCHAR(36)  NOT NULL,
    channel        VARCHAR(20)  NOT NULL,
    event_type     VARCHAR(100) NOT NULL,
    message        TEXT         NOT NULL,
    recipient      VARCHAR(255) NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    failure_reason VARCHAR(500),
    attempt_count  INTEGER      NOT NULL DEFAULT 0,
    trace_id       VARCHAR(100),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    sent_at        TIMESTAMPTZ,

    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT chk_channel
    CHECK (channel IN ('SMS', 'EMAIL', 'PUSH')),
    CONSTRAINT chk_status
    CHECK (status IN ('PENDING','SENT','DELIVERED','FAILED'))
    );

CREATE INDEX IF NOT EXISTS idx_notifications_recipient
    ON notifications (recipient_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notifications_status
    ON notifications (status);
CREATE INDEX IF NOT EXISTS idx_notifications_trace
    ON notifications (trace_id);