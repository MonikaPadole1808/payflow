# PayFlow Database Design

> **Project:** PayFlow
> **Document:** Database Design
> **Version:** 1.0
> **Status:** Active
> **Created:** 18-Jul-2026

---

# 1. Purpose

This document defines the database architecture for PayFlow Version 1.

It acts as the single source of truth for:

- Database design principles
- Naming conventions
- Table ownership
- Relationships
- Constraints
- Index strategy
- Migration strategy
- Data integrity rules

Entity implementation details are intentionally excluded.

---

# 2. Database Overview

## Database Engine

PostgreSQL

---

## Version

Latest Stable Version

---

## Database Name

payflow_db

---

## Migration Tool

Flyway

---

## ORM

Spring Data JPA (Hibernate)

---

## Character Set

UTF-8

---

## Time Zone

UTC

(All timestamps stored in UTC.)

---

# 3. Database Design Principles

The following principles are mandatory.

---

## Single Source of Truth

Every business entity is stored exactly once.

Duplicate business data is prohibited.

---

## Module Ownership

Every table has exactly one owning module.

Only the owning module is responsible for:

- Create
- Update
- Delete

Other modules may only read through service interfaces.

---

## Normalization

Version 1 follows Third Normal Form (3NF).

Data duplication should be minimized.

---

## Referential Integrity

Foreign Keys shall be used where appropriate.

Orphan records are prohibited.

---

## Immutable History

Business transactions should never be physically deleted.

Historical records are preserved.

---

## Soft Delete

Version 1 will not implement soft delete.

Future versions may introduce logical deletion where required.

---

## Auditability

Every business table should support auditing.

Typical audit fields:

- created_at
- created_by
- updated_at
- updated_by

---

# 4. Naming Conventions

## Tables

Plural

Examples

users

wallets

payments

transactions

---

## Columns

snake_case

Examples

first_name

created_at

wallet_balance

---

## Primary Key

id

UUID

---

## Foreign Key

<Table>_id

Examples

user_id

wallet_id

payment_id

---

## Constraints

Descriptive names.

Example

fk_wallet_user

uk_user_email

---

## Indexes

Descriptive names.

Example

idx_transaction_status

idx_wallet_user

---

# 5. Table Ownership

| Table | Owner Module |
|--------|--------------|
| users | user |
| wallets | wallet |
| payments | payment |
| transactions | transaction |
| notifications | notification |
| refresh_tokens | auth |

Cross-module ownership is prohibited.

---

# 6. Relationships

Current Version

One User

↓

One Wallet

One Wallet

↓

Many Transactions

One Payment

↓

One Transaction Record

Additional relationships will be documented as features are implemented.

---

# 7. Transaction Management

Business operations requiring multiple table updates must execute inside database transactions.

Spring @Transactional shall be used where appropriate.

Partial updates are prohibited.

---

# 8. Migration Strategy

Database changes are managed exclusively through Flyway.

Rules

- Never modify an executed migration.
- Every schema change requires a new migration.
- Migrations are immutable.
- Migration history must remain consistent across environments.

---

# 9. Index Strategy

Indexes will be created only when required.

Initial candidates

- email
- username
- transaction_id
- wallet_id
- payment_reference

Avoid unnecessary indexes.

---

# 10. Constraints

Examples

NOT NULL

UNIQUE

CHECK

FOREIGN KEY

Business constraints belong in both:

- Database
- Application

---

# 11. UUID Strategy

All primary keys use UUID.

Reasons

- Globally unique
- Future microservice compatibility
- Safer data merging
- Easier distributed architecture

---

# 12. Audit Fields

Every business table should contain:

created_at

updated_at

created_by

updated_by

Version 1 may populate only timestamps.

Future versions can integrate Spring Data Auditing.

---

# 13. Initial Tables

Version 1 starts with:

users

wallets

payments

transactions

refresh_tokens

Each table will receive its own detailed specification before implementation.

---

# 14. Future Tables

Not part of Version 1.

merchant

refund

settlement

scheduler

audit_log

notification_log

payment_gateway

outbox_event

inbox_event

---

# 15. Performance Guidelines

Avoid N+1 queries.

Fetch only required columns.

Prefer pagination.

Avoid unnecessary eager loading.

Optimize before introducing caching.

---

# 16. Backup Strategy

Development

No backup policy.

Production (Future)

Daily backups

Point-in-time recovery

Disaster recovery strategy

---

# 17. Security

Passwords

Never stored in plain text.

Sensitive data

Encrypted where necessary.

Credentials

Never stored inside business tables.

---

# 18. Revision History

| Version | Date | Description |
|----------|------|-------------|
| 1.0 | 18-Jul-2026 | Initial database design |

---

# 19. Future Updates

The following sections will be completed incrementally as features are implemented:

- Detailed table definitions
- ER Diagram
- Column specifications
- Foreign Keys
- Constraints
- Indexes
- Sample data
- Migration history

This document evolves together with the database schema.

Design principles defined above remain unchanged.

---

# 20. Implemented Table Details

## wallets

Owner Module

wallet

Purpose

Stores the current wallet balance for each authenticated user.

Relationship

One user owns exactly one wallet.

Columns

| Column | Type | Rule |
|--------|------|------|
| id | UUID | Primary key |
| user_id | UUID | Required, unique, references users(id) |
| balance | NUMERIC(19,2) | Required, default 0.00, cannot be negative |
| currency | VARCHAR(3) | Required, default INR |
| status | VARCHAR(30) | Required, ACTIVE or BLOCKED |
| created_at | TIMESTAMPTZ | Required |
| updated_at | TIMESTAMPTZ | Required |

Constraints

- `uk_wallets_user_id`
- `fk_wallets_user`
- `ck_wallets_balance_non_negative`
- `ck_wallets_currency_length`
- `ck_wallets_status`

Migration

- `V4__create_wallets_table.sql`

---

## transactions

Owner Module

transaction

Purpose

Stores immutable ledger records for successful wallet balance movements.

Relationship

One wallet has many transactions.

Columns

| Column | Type | Rule |
|--------|------|------|
| id | UUID | Primary key |
| wallet_id | UUID | Required, references wallets(id) |
| transaction_type | VARCHAR(30) | Required, DEPOSIT, WITHDRAW, TRANSFER_OUT, or TRANSFER_IN |
| status | VARCHAR(30) | Required, SUCCESS |
| amount | NUMERIC(19,2) | Required, must be positive |
| balance_before | NUMERIC(19,2) | Required, cannot be negative |
| balance_after | NUMERIC(19,2) | Required, cannot be negative |
| currency | VARCHAR(3) | Required |
| reference_number | VARCHAR(64) | Required, unique |
| description | VARCHAR(255) | Required |
| created_at | TIMESTAMPTZ | Required |

Constraints

- `fk_transactions_wallet`
- `uk_transactions_reference_number`
- `ck_transactions_type`
- `ck_transactions_status`
- `ck_transactions_amount_positive`
- `ck_transactions_balance_before_non_negative`
- `ck_transactions_balance_after_non_negative`
- `ck_transactions_currency_length`

Indexes

- `idx_transactions_wallet_created_at`

Migration

- `V5__create_transactions_table.sql`

Rules

- Transaction records are immutable.
- Transactions are never updated.
- Transactions are never deleted.
- Deposit and withdraw operations create exactly one SUCCESS transaction after a successful wallet balance change.
- Wallet-to-wallet transfers create one TRANSFER_OUT transaction for the sender and one TRANSFER_IN transaction for the receiver.

---

## payments

Owner Module

payment

Purpose

Stores successful wallet-to-wallet payment records.

Relationship

One sender wallet and one receiver wallet are linked to each payment.

Columns

| Column | Type | Rule |
|--------|------|------|
| id | UUID | Primary key |
| sender_wallet_id | UUID | Required, references wallets(id) |
| receiver_wallet_id | UUID | Required, references wallets(id) |
| amount | NUMERIC(19,2) | Required, must be positive |
| currency | VARCHAR(3) | Required |
| status | VARCHAR(30) | Required, SUCCESS |
| reference_number | VARCHAR(64) | Required, unique |
| description | VARCHAR(255) | Required |
| created_at | TIMESTAMPTZ | Required |

Constraints

- `fk_payments_sender_wallet`
- `fk_payments_receiver_wallet`
- `uk_payments_reference_number`
- `ck_payments_distinct_wallets`
- `ck_payments_amount_positive`
- `ck_payments_currency_length`
- `ck_payments_status`

Indexes

- `idx_payments_sender_wallet_created_at`
- `idx_payments_receiver_wallet_created_at`

Migration

- `V6__create_payments_table.sql`

Rules

- Payment records are created only for successful wallet-to-wallet transfers.
- A user cannot transfer money to themselves.
- Every successful payment creates exactly one payment record.
- Every successful payment creates exactly two transaction ledger records.

---

## notifications

Owner Module

notification

Purpose

Stores notification history for successful business events.

Relationship

One user has many notifications.

Columns

| Column | Type | Rule |
|--------|------|------|
| id | UUID | Primary key |
| user_id | UUID | Required, references users(id) |
| notification_type | VARCHAR(30) | Required, REGISTRATION, DEPOSIT, WITHDRAW, PAYMENT_SENT, or PAYMENT_RECEIVED |
| status | VARCHAR(30) | Required, UNREAD or READ |
| title | VARCHAR(120) | Required, cannot be blank |
| message | VARCHAR(500) | Required, cannot be blank |
| reference_number | VARCHAR(64) | Required, cannot be blank |
| created_at | TIMESTAMPTZ | Required |
| read_at | TIMESTAMPTZ | Required when status is READ, null when status is UNREAD |

Constraints

- `fk_notifications_user`
- `ck_notifications_type`
- `ck_notifications_status`
- `ck_notifications_read_state`
- `ck_notifications_title_not_blank`
- `ck_notifications_message_not_blank`
- `ck_notifications_reference_number_not_blank`

Indexes

- `idx_notifications_user_created_at`

Migration

- `V7__create_notifications_table.sql`

Rules

- Notifications are created internally only after successful business operations.
- Users can access only their own notifications.
- Notifications are never deleted.
- Mark-as-read is idempotent.

---

## admin

Owner Module

admin

Purpose

Provides administrative visibility and orchestration across existing modules.

Database Impact

No admin-owned database tables are created in Version 1.

Rules

- Admin module owns no business data.
- Admin module must not access repositories owned by other modules directly.
- Admin reads and modifies data only through owning module services.

Migration

None.

---

# 21. Approval

Status

ACTIVE

This document is the official database reference for PayFlow Version 1.

All schema modifications must be reflected here before or alongside implementation.
