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

# 20. Approval

Status

ACTIVE

This document is the official database reference for PayFlow Version 1.

All schema modifications must be reflected here before or alongside implementation.