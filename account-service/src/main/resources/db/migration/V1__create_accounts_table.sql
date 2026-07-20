CREATE TABLE IF NOT EXISTS accounts
(
    id             VARCHAR(36)  NOT NULL,
    account_number VARCHAR(10)  NOT NULL,
    bank_code      VARCHAR(3)   NOT NULL,
    owner_bvn      VARCHAR(11)  NOT NULL,
    account_type   VARCHAR(20)  NOT NULL,
    currency       VARCHAR(3)   NOT NULL DEFAULT 'NGN',
    balance_kobo   BIGINT       NOT NULL DEFAULT 0,
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING_KYC',

    -- Optimistic locking column.
    -- Hibernate automatically increments this on every UPDATE.
    -- Prevents concurrent double-spend without database locks.
    version        BIGINT       NOT NULL DEFAULT 0,

    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_accounts             PRIMARY KEY (id),
    CONSTRAINT uq_accounts_number      UNIQUE (account_number),
    CONSTRAINT chk_balance_non_negative CHECK (balance_kobo >= 0)
    );

-- Index for account number lookups (every transaction uses this)
CREATE INDEX IF NOT EXISTS idx_accounts_number
    ON accounts (account_number);

-- Index for finding all accounts by BVN (customer dashboard)
CREATE INDEX IF NOT EXISTS idx_accounts_owner_bvn
    ON accounts (owner_bvn);

-- Index for finding accounts by status (admin queries)
CREATE INDEX IF NOT EXISTS idx_accounts_status
    ON accounts (status);

CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_accounts_updated_at
    BEFORE UPDATE ON accounts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();