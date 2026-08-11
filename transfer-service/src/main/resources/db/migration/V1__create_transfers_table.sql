CREATE TABLE IF NOT EXISTS transfers
(
    id                         VARCHAR(50)  NOT NULL,
    idempotency_key            VARCHAR(100) NOT NULL,
    source_account_number      VARCHAR(10)  NOT NULL,
    destination_account_number VARCHAR(10)  NOT NULL,
    destination_bank_code      VARCHAR(3)   NOT NULL,
    amount_kobo                BIGINT       NOT NULL,
    currency                   VARCHAR(3)   NOT NULL DEFAULT 'NGN',
    narration                  VARCHAR(200),
    initiated_by_user_id       VARCHAR(36)  NOT NULL,

    -- SAGA state — updated on every step
    -- PENDING | PROCESSING | COMPLETED | FAILED | REVERSED
    status                     VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    failure_reason             VARCHAR(500),

    created_at                 TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at                 TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    completed_at               TIMESTAMPTZ,

    CONSTRAINT pk_transfers              PRIMARY KEY (id),
    CONSTRAINT uq_transfers_idempotency  UNIQUE (idempotency_key),
    CONSTRAINT chk_amount_positive       CHECK (amount_kobo > 0)
    );

CREATE INDEX IF NOT EXISTS idx_transfers_source
    ON transfers (source_account_number);
CREATE INDEX IF NOT EXISTS idx_transfers_status
    ON transfers (status);
CREATE INDEX IF NOT EXISTS idx_transfers_created
    ON transfers (created_at DESC);

CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_transfers_updated_at
    BEFORE UPDATE ON transfers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();