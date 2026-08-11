CREATE TABLE IF NOT EXISTS ledger_entries
(
    id                         VARCHAR(36)   NOT NULL,
    transaction_reference      VARCHAR(100)  NOT NULL,
    account_number             VARCHAR(10)   NOT NULL,
    entry_type                 VARCHAR(10)   NOT NULL,

    -- Always in kobo (smallest denomination). Never DECIMAL.
    amount_kobo                BIGINT        NOT NULL,
    currency                   VARCHAR(3)    NOT NULL DEFAULT 'NGN',

    description                VARCHAR(500)  NOT NULL,
    counterpart_account_number VARCHAR(10),

    -- IMMUTABLE — no updated_at column. Entries never change.
    created_at                 TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_ledger_entries PRIMARY KEY (id),
    CONSTRAINT chk_entry_type    CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    CONSTRAINT chk_amount_positive CHECK (amount_kobo > 0)
    );

-- Primary query path: find entries for an account sorted by date
CREATE INDEX IF NOT EXISTS idx_ledger_account_date
    ON ledger_entries (account_number, created_at DESC);

-- Used for idempotency checks
CREATE INDEX IF NOT EXISTS idx_ledger_reference
    ON ledger_entries (transaction_reference);

-- Used for reconciliation queries (sum all debits/credits)
CREATE INDEX IF NOT EXISTS idx_ledger_entry_type
    ON ledger_entries (entry_type);