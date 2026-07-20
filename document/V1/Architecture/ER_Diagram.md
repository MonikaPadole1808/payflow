# PayFlow V1 ER Diagram

```mermaid
erDiagram
    USERS {
        UUID id PK
        VARCHAR email UK
        VARCHAR password_hash
        VARCHAR role
        VARCHAR status
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    REFRESH_TOKENS {
        UUID id PK
        UUID user_id FK
        VARCHAR token UK
        TIMESTAMPTZ expires_at
        TIMESTAMPTZ created_at
    }

    WALLETS {
        UUID id PK
        UUID user_id FK,UK
        NUMERIC balance
        VARCHAR currency
        VARCHAR status
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    TRANSACTIONS {
        UUID id PK
        UUID wallet_id FK
        VARCHAR transaction_type
        VARCHAR status
        NUMERIC amount
        NUMERIC balance_before
        NUMERIC balance_after
        VARCHAR currency
        VARCHAR reference_number UK
        VARCHAR description
        TIMESTAMPTZ created_at
    }

    PAYMENTS {
        UUID id PK
        UUID sender_wallet_id FK
        UUID receiver_wallet_id FK
        NUMERIC amount
        VARCHAR currency
        VARCHAR status
        VARCHAR reference_number UK
        VARCHAR description
        TIMESTAMPTZ created_at
    }

    NOTIFICATIONS {
        UUID id PK
        UUID user_id FK
        VARCHAR notification_type
        VARCHAR status
        VARCHAR title
        VARCHAR message
        VARCHAR reference_number
        TIMESTAMPTZ created_at
        TIMESTAMPTZ read_at
    }

    USERS ||--|| WALLETS : owns
    USERS ||--o{ REFRESH_TOKENS : receives
    USERS ||--o{ NOTIFICATIONS : receives
    WALLETS ||--o{ TRANSACTIONS : records
    WALLETS ||--o{ PAYMENTS : sends
    WALLETS ||--o{ PAYMENTS : receives
```

## Relationship rules

- A user owns exactly one wallet.
- A user may have multiple refresh tokens and notifications.
- A wallet may have multiple immutable ledger transactions.
- A payment links one sender wallet and one receiver wallet.
- The Admin module owns no database table.
