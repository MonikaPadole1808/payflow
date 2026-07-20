# Transaction Ledger Module

## Milestone

04 - Transaction Ledger

---

## Objective

Implement the Transaction Ledger module.

The ledger records every financial movement within the system.

This module is the permanent source of truth for account activity.

The Payment module will use this infrastructure in future milestones.

---

## Prerequisites

Completed

- Project Foundation
- Authentication Foundation
- Wallet Module

---

## Scope

Implement only Transaction Ledger functionality.

Included

- Transaction Entity
- Transaction Repository
- Transaction Service
- Transaction Controller
- Transaction DTOs
- Flyway Migration
- Transaction Tests

---

## Functional Requirements

### Record Deposit

Every successful deposit must create one transaction record.

---

### Record Withdrawal

Every successful withdrawal must create one transaction record.

---

### Transaction History

Authenticated users can retrieve their own transaction history.

Transactions must be ordered from newest to oldest.

---

### Transaction Details

Authenticated users can retrieve one transaction by ID.

Users may access only their own transactions.

---

## Transaction Types

Version 1 supports

- DEPOSIT
- WITHDRAW

Future versions will add

- TRANSFER_IN
- TRANSFER_OUT
- PAYMENT
- REFUND
- REVERSAL

---

## Transaction Status

Version 1

- SUCCESS

Future

- PENDING
- FAILED
- CANCELLED
- REVERSED

---

## Database

Create

transactions

Relationship

User (1)

↓

Wallet (1)

↓

Many Transactions

Each transaction belongs to exactly one wallet.

---

## Fields

Minimum required

- id
- wallet_id
- transaction_type
- status
- amount
- balance_before
- balance_after
- currency
- reference_number
- description
- created_at

Additional fields may be added only if required.

---

## Business Rules

Every successful balance change creates exactly one transaction.

Transaction records are immutable.

Transactions are never updated.

Transactions are never deleted.

Historical data must remain intact.

---

## API Endpoints

GET

/api/v1/transactions

Return authenticated user's transaction history.

---

GET

/api/v1/transactions/{id}

Return transaction details.

---

No transaction creation endpoint.

Transactions are created internally by business services.

---

## Validation

Users may access only their own transactions.

Amounts must always be positive.

Balance snapshots must match the wallet state at the time of the operation.

---

## Integration

Deposit

↓

Wallet Updated

↓

Transaction Recorded

Withdraw

↓

Wallet Updated

↓

Transaction Recorded

Payment integration will be implemented later.

---

## Constraints

Do NOT implement

- Wallet-to-Wallet Transfer
- Payment Processing
- Refund
- Notifications
- Scheduler
- Admin Transaction APIs

---

## Deliverables

- Transaction Entity
- Transaction Repository
- Transaction Service
- Transaction Controller
- Transaction DTOs
- Flyway Migration
- Unit Tests
- Integration Tests
- Documentation Updates

---

## Documentation Updates

Update

- API_Contract.md
- Database_Design.md
- Project_Progress.md

if implementation introduces new information.

---

## Success Criteria

Application builds successfully.

All tests pass.

Deposit creates a transaction.

Withdraw creates a transaction.

Transaction history works.

Architecture remains unchanged.

Git patch generated for review.