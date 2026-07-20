CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id UUID NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    balance_before NUMERIC(19, 2) NOT NULL,
    balance_after NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    reference_number VARCHAR(64) NOT NULL,
    description VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_transactions_wallet FOREIGN KEY (wallet_id) REFERENCES wallets (id),
    CONSTRAINT uk_transactions_reference_number UNIQUE (reference_number),
    CONSTRAINT ck_transactions_type CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAW')),
    CONSTRAINT ck_transactions_status CHECK (status IN ('SUCCESS')),
    CONSTRAINT ck_transactions_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_transactions_balance_before_non_negative CHECK (balance_before >= 0),
    CONSTRAINT ck_transactions_balance_after_non_negative CHECK (balance_after >= 0),
    CONSTRAINT ck_transactions_currency_length CHECK (char_length(currency) = 3)
);

CREATE INDEX idx_transactions_wallet_created_at ON transactions (wallet_id, created_at DESC);
