# PayFlow API Contract

> **Project:** PayFlow
> **Document:** API Contract
> **Version:** 1.0
> **Status:** Frozen (Until API Changes)
> **Created:** 18-Jul-2026

---

# 1. Purpose

This document defines the REST API contract for PayFlow Version 1.

It serves as the agreement between backend services and API consumers.

This document specifies:

- Endpoint definitions
- Request format
- Response format
- Validation rules
- Authentication requirements
- Authorization requirements
- HTTP status codes
- Error codes

Implementation details are intentionally excluded.

---

# 2. API Version

Current Version

V1

Base URL

/api/v1

Example

/api/v1/auth/login

---

# 3. Content Type

Request

application/json

Response

application/json

Character Encoding

UTF-8

---

# 4. Authentication

Authentication Mechanism

JWT Bearer Token

Header

Authorization

Format

Bearer <JWT_TOKEN>

Example

Authorization: Bearer eyJhbGciOi...

---

# 5. Standard Response Format

Every API returns a standard response.

Example Success

{
"success": true,
"message": "Operation completed successfully.",
"data": { }
}

Example Error

{
"success": false,
"message": "Validation failed.",
"errors": [
...
]
}

Controllers must never return entities directly.

---

# 6. HTTP Status Codes

200 OK

Successful request

201 Created

Resource created

204 No Content

Successful request without response body

400 Bad Request

Validation failed

401 Unauthorized

Authentication failed

403 Forbidden

Authorization failed

404 Not Found

Requested resource not found

409 Conflict

Business conflict

422 Unprocessable Entity

Business validation failure

500 Internal Server Error

Unexpected server error

---

# 7. Common Validation Rules

All incoming requests must be validated.

Validation occurs before business logic execution.

Invalid requests never reach Service layer.

---

# 8. Error Handling

Errors follow a standard structure.

Stack traces are never exposed.

Business exceptions return meaningful messages.

Unexpected exceptions return generic responses.

---

# 9. Endpoint Categories

Public APIs

Protected APIs

Admin APIs

---

# 10. Public APIs

Authentication is NOT required.

Included

POST /auth/register

POST /auth/login

POST /auth/refresh

GET /actuator/health

---

# 11. Protected APIs

Authentication Required

Authorization

USER

ADMIN

Included

User

Wallet

Payment

Transaction

---

# 12. Admin APIs

Authentication Required

Authorization

ADMIN

Only administrators may access these endpoints.

---

# 13. Authentication APIs

## Register

POST

/api/v1/auth/register

Authentication

Not Required

Purpose

Register a new user.

Request

(To be added during implementation.)

Response

(To be added during implementation.)

Validation

(To be added.)

---

## Login

POST

/api/v1/auth/login

Authentication

Not Required

Purpose

Authenticate user.

---

## Refresh Token

POST

/api/v1/auth/refresh

Authentication

Not Required

Purpose

Generate new access token.

---

# 14. User APIs

GET /users/me

Current User Profile

PUT /users/me

Update Profile

---

# 15. Wallet APIs

All Wallet APIs require authentication.

Authorization

USER

ADMIN

---

## Wallet Details

GET

/api/v1/wallet

Wallet Details

Response

{
"success": true,
"message": "Wallet retrieved successfully",
"data": {
"id": "uuid",
"userId": "uuid",
"balance": 0.00,
"currency": "INR",
"status": "ACTIVE",
"createdAt": "timestamp",
"updatedAt": "timestamp"
}
}

---

## Wallet Deposit

POST

/api/v1/wallet/deposit

Deposit funds into the authenticated user's wallet.

Request

{
"amount": 100.00
}

Validation

- amount is required
- amount must be greater than 0
- amount must have up to 17 integer digits and 2 decimal places

---

## Wallet Withdraw

POST

/api/v1/wallet/withdraw

Withdraw Money

Request

{
"amount": 100.00
}

Validation

- amount is required
- amount must be greater than 0
- amount must have up to 17 integer digits and 2 decimal places
- wallet balance must be sufficient

---

# 16. Payment APIs

All Payment APIs require authentication.

Authorization

USER

ADMIN

---

## Transfer Money

POST

/api/v1/payments/transfer

Transfer Money

Purpose

Transfer money from the authenticated user's wallet to another user's wallet.

Request

{
"receiverUserId": "uuid",
"amount": 100.00,
"description": "Wallet transfer"
}

Validation

- receiverUserId is required
- amount is required
- amount must be greater than 0
- amount must have up to 17 integer digits and 2 decimal places
- description must not exceed 255 characters
- sender and receiver must be different users
- sender wallet balance must be sufficient

Response

{
"success": true,
"message": "Payment transfer completed successfully",
"data": {
"id": "uuid",
"senderWalletId": "uuid",
"receiverWalletId": "uuid",
"amount": 100.00,
"currency": "INR",
"status": "SUCCESS",
"referenceNumber": "PAY-uuid",
"description": "Wallet transfer",
"createdAt": "timestamp"
}
}

Rules

- Transfer processing is atomic.
- A successful transfer creates one payment record.
- A successful transfer creates one TRANSFER_OUT ledger record for the sender.
- A successful transfer creates one TRANSFER_IN ledger record for the receiver.
- No external payment gateway is used in Version 1.

---

## Payment History

GET

/api/v1/payments

Purpose

Return payments where the authenticated user is the sender or receiver.

Response

{
"success": true,
"message": "Payments retrieved successfully",
"data": [
{
"id": "uuid",
"senderWalletId": "uuid",
"receiverWalletId": "uuid",
"amount": 100.00,
"currency": "INR",
"status": "SUCCESS",
"referenceNumber": "PAY-uuid",
"description": "Wallet transfer",
"createdAt": "timestamp"
}
]
}

Rules

- Payments are returned newest first.
- Users can view only payments where they are the sender or receiver.

---

## Payment Details

GET

/api/v1/payments/{id}

Purpose

Return one payment owned by the authenticated user.

Response

{
"success": true,
"message": "Payment retrieved successfully",
"data": {
"id": "uuid",
"senderWalletId": "uuid",
"receiverWalletId": "uuid",
"amount": 100.00,
"currency": "INR",
"status": "SUCCESS",
"referenceNumber": "PAY-uuid",
"description": "Wallet transfer",
"createdAt": "timestamp"
}
}

Rules

- Users can view only payments where they are the sender or receiver.

---

# 17. Transaction APIs

All Transaction APIs require authentication.

Authorization

USER

ADMIN

---

## Transaction History

GET

/api/v1/transactions

Transaction History

Purpose

Return the authenticated user's transaction history.

Response

{
"success": true,
"message": "Transactions retrieved successfully",
"data": [
{
"id": "uuid",
"walletId": "uuid",
"transactionType": "DEPOSIT",
"status": "SUCCESS",
"amount": 100.00,
"balanceBefore": 0.00,
"balanceAfter": 100.00,
"currency": "INR",
"referenceNumber": "TXN-uuid",
"description": "Wallet deposit",
"createdAt": "timestamp"
}
]
}

Rules

- Transactions are returned newest first.
- Users can view only their own transactions.
- Version 1 supports DEPOSIT, WITHDRAW, TRANSFER_OUT, and TRANSFER_IN transaction types.

---

## Transaction Details

GET

/api/v1/transactions/{id}

Transaction Details

Purpose

Return one transaction owned by the authenticated user.

Response

{
"success": true,
"message": "Transaction retrieved successfully",
"data": {
"id": "uuid",
"walletId": "uuid",
"transactionType": "WITHDRAW",
"status": "SUCCESS",
"amount": 50.00,
"balanceBefore": 100.00,
"balanceAfter": 50.00,
"currency": "INR",
"referenceNumber": "TXN-uuid",
"description": "Wallet withdrawal",
"createdAt": "timestamp"
}
}

Rules

- Users can view only their own transactions.
- Transactions are created internally only.
- No public create transaction API exists in Version 1.

---

# 18. Notification APIs

No public APIs in Version 1.

---

# 19. Admin APIs

Reserved for future implementation.

---

# 20. Error Codes

Business error codes will be defined in one centralized enum.

Examples

AUTH_001

USER_001

WALLET_001

PAYMENT_001

TRANSACTION_001

Validation error codes will follow the same convention.

---

# 21. Future Enhancements

The following APIs are intentionally excluded from Version 1.

Merchant APIs

Refund APIs

Settlement APIs

Scheduler APIs

Webhook APIs

Payment Gateway APIs

Kafka Event APIs

---

# 22. Revision History

| Version | Date | Description |
|----------|------|-------------|
| 1.0 | 18-Jul-2026 | Initial API Contract |

---

# 23. Notes

This document defines API contracts only.

Actual request payloads, response payloads, and validation constraints will be completed incrementally as each feature is implemented.

Any API contract modification requires updating this document before code implementation.
