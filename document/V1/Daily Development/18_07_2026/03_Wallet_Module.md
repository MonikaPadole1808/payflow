# Wallet Module

## Milestone

03 - Wallet Module

---

## Objective

Implement the Wallet module for PayFlow Version 1.

This is the first business module and will become the foundation for future Payment and Transaction modules.

The implementation must strictly follow the frozen architecture documents.

---

## Prerequisites

Authentication Foundation is completed.

Available infrastructure:

- Spring Security
- JWT Authentication
- Global Exception Handling
- ApiResponse
- Flyway
- User Module
- Authentication Module

---

## Scope

Implement only Wallet functionality.

Included:

- Wallet Entity
- Wallet Repository
- Wallet Service
- Wallet Controller
- Wallet DTOs
- Wallet Validation
- Wallet Flyway Migration
- Wallet Unit Tests

---

## Functional Requirements

### Wallet Creation

A wallet shall be created automatically after successful user registration.

Each user owns exactly one wallet.

---

### Balance Inquiry

Authenticated users can retrieve their wallet balance.

---

### Deposit

Authenticated users can deposit money into their own wallet.

Validation required.

---

### Withdraw

Authenticated users can withdraw money.

Insufficient balance must be rejected.

Balance must never become negative.

---

## Database

Create:

wallets

Table ownership:

Wallet Module

Relationship:

User (1)

↓

Wallet (1)

---

## API Endpoints

GET

/api/v1/wallet

Retrieve wallet details.

POST

/api/v1/wallet/deposit

Deposit funds.

POST

/api/v1/wallet/withdraw

Withdraw funds.

---

## Validation

Validate:

- Amount > 0
- Wallet exists
- User owns wallet
- Sufficient balance

---

## Business Rules

One user can own only one wallet.

Balance cannot become negative.

Money values must use BigDecimal.

Do not use floating-point types.

---

## Constraints

Do NOT implement:

- Payment
- Transaction
- Notification
- Scheduler
- Merchant
- Transfer

Focus only on Wallet.

---

## Deliverables

- Wallet Entity
- Wallet Repository
- Wallet Service
- Wallet Controller
- Wallet DTOs
- Wallet Migration
- Wallet Tests
- Documentation updates

---

## Documentation Updates

Update:

- API_Contract.md
- Database_Design.md
- Project_Progress.md

if implementation introduces new information.

---

## Success Criteria

- Application builds successfully.
- Tests pass.
- Wallet APIs function correctly.
- Automatic wallet creation works.
- Architecture remains unchanged.