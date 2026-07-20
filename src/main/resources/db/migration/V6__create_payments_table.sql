ALTER TABLE transactions DROP CONSTRAINT ck_transactions_type;

ALTER TABLE transactions
    ADD CONSTRAINT ck_transactions_type
        CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAW', 'TRANSFER_OUT', 'TRANSFER_IN'));

CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_wallet_id UUID NOT NULL,
    receiver_wallet_id UUID NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL,
    reference_number VARCHAR(64) NOT NULL,
    description VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_payments_sender_wallet FOREIGN KEY (sender_wallet_id) REFERENCES wallets (id),
    CONSTRAINT fk_payments_receiver_wallet FOREIGN KEY (receiver_wallet_id) REFERENCES wallets (id),
    CONSTRAINT uk_payments_reference_number UNIQUE (reference_number),
    CONSTRAINT ck_payments_distinct_wallets CHECK (sender_wallet_id <> receiver_wallet_id),
    CONSTRAINT ck_payments_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_payments_currency_length CHECK (char_length(currency) = 3),
    CONSTRAINT ck_payments_status CHECK (status IN ('SUCCESS'))
);

CREATE INDEX idx_payments_sender_wallet_created_at ON payments (sender_wallet_id, created_at DESC);
CREATE INDEX idx_payments_receiver_wallet_created_at ON payments (receiver_wallet_id, created_at DESC);
