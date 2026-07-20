# Payment Processing Module

## Milestone

05 - Payment Processing

---

## Objective

Implement wallet-to-wallet payment processing for PayFlow Version 1.

The Payment module orchestrates a transfer between two wallets by coordinating the Wallet and Transaction modules.

This module must not own wallet balances or transaction persistence.

---

## Prerequisites

Completed

- Project Foundation
- Authentication Foundation
- Wallet Module
- Transaction Ledger Module

---

## Scope

Implement only payment processing.

Included

- Payment Entity
- Payment Repository
- Payment Service
- Payment Controller
- Payment DTOs
- Flyway Migration
- Payment Tests

---

## Functional Requirements

### Transfer Money

An authenticated user transfers money from their wallet to another user's wallet.

---

### Atomic Processing

A transfer must execute as one database transaction.

Either every step succeeds or the operation is rolled back.

---

### Ledger Recording

A successful transfer must create:

- One TRANSFER_OUT transaction for the sender
- One TRANSFER_IN transaction for the receiver

No duplicate ledger records are allowed.

---

### Payment Record

Store one payment record containing:

- Sender wallet
- Receiver wallet
- Amount
- Currency
- Reference number
- Status
- Created timestamp

---

## Database

Create

payments

Relationship

Wallet (Sender)

↓

Payment

↓

Wallet (Receiver)

---

## Minimum Fields

- id
- sender_wallet_id
- receiver_wallet_id
- amount
- currency
- status
- reference_number
- description
- created_at

---

## Payment Status

Version 1

- SUCCESS

Future

- PENDING
- FAILED
- CANCELLED
- REVERSED

---

## Business Rules

Users cannot transfer to themselves.

Transfer amount must be positive.

Sender must have sufficient balance.

Receiver wallet must exist.

Every successful transfer creates exactly:

- One payment record
- One sender ledger entry
- One receiver ledger entry

No partial updates.

---

## API Endpoints

### Transfer Money

POST

/api/v1/payments/transfer

Authentication

Required

---

### Payment History

GET

/api/v1/payments

Return authenticated user's payments.

---

### Payment Details

GET

/api/v1/payments/{id}

Return one payment owned by the authenticated user.

---

## Validation

- Positive amount
- Sender wallet exists
- Receiver wallet exists
- Sender != Receiver
- Sufficient balance

---

## Transaction Management

The complete payment flow must execute inside one @Transactional boundary.

Flow

Sender Wallet

↓

Validate

↓

Debit Sender

↓

Credit Receiver

↓

Create Payment

↓

Record TRANSFER_OUT

↓

Record TRANSFER_IN

↓

Commit

---

## Constraints

Do NOT implement

- External payment gateway
- Merchant payments
- Refunds
- Scheduled payments
- Notifications
- Kafka events

---

## Deliverables

- Payment Entity
- Payment Repository
- Payment Service
- Payment Controller
- Payment DTOs
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

when implementation introduces new information.

---

## Success Criteria

- Application builds successfully.
- All tests pass.
- Wallet-to-wallet transfer works.
- Sender balance updated.
- Receiver balance updated.
- One payment record created.
- Two immutable ledger entries created.
- Architecture remains unchanged.
- Git review package generated.