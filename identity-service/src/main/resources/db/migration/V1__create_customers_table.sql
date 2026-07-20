CREATE TABLE IF NOT EXISTS customers
(
    id               VARCHAR(36)  NOT NULL,
    auth_user_id     VARCHAR(36)  NOT NULL,
    bvn              VARCHAR(11)  NOT NULL,
    nin              VARCHAR(11),
    first_name       VARCHAR(100) NOT NULL,
    last_name        VARCHAR(100) NOT NULL,
    middle_name      VARCHAR(100),
    date_of_birth    DATE         NOT NULL,
    phone_number     VARCHAR(20)  NOT NULL,
    email            VARCHAR(255),
    address          VARCHAR(500),
    state_of_origin  VARCHAR(100),
    kyc_status       VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    kyc_rejection_reason VARCHAR(500),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_customers         PRIMARY KEY (id),
    CONSTRAINT uq_customers_bvn     UNIQUE (bvn),
    CONSTRAINT uq_customers_nin     UNIQUE (nin),
    CONSTRAINT uq_customers_auth_id UNIQUE (auth_user_id)
    );

CREATE TABLE IF NOT EXISTS kyc_documents
(
    id                 VARCHAR(36)  NOT NULL,
    customer_id        VARCHAR(36)  NOT NULL,
    document_type      VARCHAR(50)  NOT NULL,
    storage_object_key VARCHAR(500) NOT NULL,
    original_filename  VARCHAR(255),
    content_type       VARCHAR(100),
    file_size_bytes    BIGINT,
    uploaded_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_kyc_documents     PRIMARY KEY (id),
    CONSTRAINT fk_kyc_docs_customer
    FOREIGN KEY (customer_id)
    REFERENCES customers (id)
    ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_customers_bvn
    ON customers (bvn);

CREATE INDEX IF NOT EXISTS idx_customers_kyc_status
    ON customers (kyc_status);

CREATE INDEX IF NOT EXISTS idx_kyc_docs_customer
    ON kyc_documents (customer_id);

CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_customers_updated_at
    BEFORE UPDATE ON customers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();