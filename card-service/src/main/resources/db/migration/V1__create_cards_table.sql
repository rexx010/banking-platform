CREATE TABLE IF NOT EXISTS cards
(
    id                  VARCHAR(36)  NOT NULL,
    card_number         VARCHAR(16)  NOT NULL,
    linked_nuban        VARCHAR(10)  NOT NULL,
    owner_user_id       VARCHAR(36)  NOT NULL,
    card_network        VARCHAR(20)  NOT NULL,
    expiry_year         INTEGER      NOT NULL,
    expiry_month        INTEGER      NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',

    -- BCrypt hash of the card PIN.
    -- CVV is deliberately absent — it is computed on demand.
    card_pin_hash       VARCHAR(255),
    spending_limit_kobo BIGINT       NOT NULL DEFAULT 0,

    issued_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_cards              PRIMARY KEY (id),
    CONSTRAINT uq_cards_number       UNIQUE (card_number),
    CONSTRAINT uq_cards_nuban        UNIQUE (linked_nuban),
    CONSTRAINT chk_card_status
    CHECK (status IN ('ACTIVE','FROZEN','BLOCKED','EXPIRED')),
    CONSTRAINT chk_expiry_month
    CHECK (expiry_month BETWEEN 1 AND 12)
    );

CREATE INDEX IF NOT EXISTS idx_cards_owner
    ON cards (owner_user_id);

CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_cards_updated_at
    BEFORE UPDATE ON cards
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();