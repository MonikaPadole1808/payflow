# PayFlow V1 API Documentation

> **Version:** 1.0.0  
> **Base URL:** `http://localhost:8080/api/v1`  
> **Authentication:** JWT Bearer token  
> **Content-Type:** `application/json`

## 1. Standard response

Successful responses use:

```json
{
  "success": true,
  "message": "Operation completed successfully.",
  "data": {}
}
```

Error responses use:

```json
{
  "success": false,
  "message": "Validation failed.",
  "errors": []
}
```

Protected requests require:

```http
Authorization: Bearer <access-token>
```

## 2. Authentication

### Register user

`POST /auth/register`

Authentication: Public

Example request:

```json
{
  "email": "user@example.com",
  "password": "Password@123"
}
```

Registration creates the user, creates the user's wallet, and records a registration notification.

### Login

`POST /auth/login`

Authentication: Public

Example request:

```json
{
  "email": "user@example.com",
  "password": "Password@123"
}
```

Store the returned access token and refresh token for later requests.

### Refresh access token

`POST /auth/refresh`

Authentication: Public

Example request:

```json
{
  "refreshToken": "<refresh-token>"
}
```

### Health check

`GET /actuator/health`

Authentication: Public.

## 3. Wallet APIs

All wallet APIs require `USER` or `ADMIN`.

### Get wallet

`GET /wallet`

Returns the authenticated user's wallet.

### Deposit

`POST /wallet/deposit`

```json
{
  "amount": 1000.00
}
```

Rules:

- Amount must be positive.
- Amount supports up to two decimal places.
- Wallet must be `ACTIVE`.
- A successful deposit creates one immutable `DEPOSIT` ledger entry and one notification.

### Withdraw

`POST /wallet/withdraw`

```json
{
  "amount": 250.00
}
```

Rules:

- Amount must be positive.
- Balance must be sufficient.
- Wallet must be `ACTIVE`.
- A successful withdrawal creates one immutable `WITHDRAW` ledger entry and one notification.

## 4. Payment APIs

All payment APIs require `USER` or `ADMIN`.

### Transfer money

`POST /payments/transfer`

```json
{
  "receiverUserId": "<receiver-user-uuid>",
  "amount": 100.00,
  "description": "Dinner payment"
}
```

Rules:

- Sender and receiver must be different users.
- Both wallets must exist and be `ACTIVE`.
- Sender must have sufficient balance.
- Processing is atomic.
- A successful transfer creates:
  - one payment record;
  - one `TRANSFER_OUT` transaction;
  - one `TRANSFER_IN` transaction;
  - one sender notification;
  - one receiver notification.

### Payment history

`GET /payments`

Returns payments in which the authenticated user is sender or receiver, newest first.

### Payment details

`GET /payments/{paymentId}`

The authenticated user must own the payment as sender or receiver.

## 5. Transaction APIs

All transaction APIs require `USER` or `ADMIN`.

### Transaction history

`GET /transactions`

Returns the authenticated user's immutable ledger entries, newest first.

Supported types:

- `DEPOSIT`
- `WITHDRAW`
- `TRANSFER_OUT`
- `TRANSFER_IN`

### Transaction details

`GET /transactions/{transactionId}`

The transaction must belong to the authenticated user's wallet.

## 6. Notification APIs

All notification APIs require `USER` or `ADMIN`.

### Notification history

`GET /notifications`

Returns the authenticated user's notifications, newest first.

Supported types:

- `REGISTRATION`
- `DEPOSIT`
- `WITHDRAW`
- `PAYMENT_SENT`
- `PAYMENT_RECEIVED`

### Notification details

`GET /notifications/{notificationId}`

### Mark notification as read

`PATCH /notifications/{notificationId}/read`

This operation is owner-scoped and idempotent.

## 7. Admin APIs

All endpoints under `/admin/**` require `ROLE_ADMIN`. A regular user receives `403 Forbidden`.

### Dashboard

`GET /admin/dashboard`

Returns:

- total users;
- active and blocked users;
- total wallets and wallet balance;
- total payments, transactions, and notifications;
- today's payment and transaction counts.

### User administration

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/admin/users?search=` | List or search users |
| GET | `/admin/users/{id}` | Get a user |
| PUT | `/admin/users/{id}` | Update email/status |
| PATCH | `/admin/users/{id}/activate` | Activate |
| PATCH | `/admin/users/{id}/deactivate` | Disable |
| PATCH | `/admin/users/{id}/block` | Disable |
| PATCH | `/admin/users/{id}/unblock` | Activate |
| GET | `/admin/users/{id}/roles` | Get role |
| PATCH | `/admin/users/{id}/roles` | Change role |

Update example:

```json
{
  "email": "updated@example.com",
  "status": "ACTIVE"
}
```

Role update:

```json
{
  "role": "ADMIN"
}
```

An administrator cannot modify their own role. Supported roles are `USER` and `ADMIN`.

### Wallet administration

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/admin/wallets?search=` | List or search wallets |
| GET | `/admin/wallets/{id}` | Get wallet |
| PATCH | `/admin/wallets/{id}/block` | Block wallet |
| PATCH | `/admin/wallets/{id}/unblock` | Unblock wallet |

Admin APIs cannot adjust wallet balances.

### Read-only financial administration

| Resource | List/search | Details |
|---|---|---|
| Payments | `GET /admin/payments?search=` | `GET /admin/payments/{id}` |
| Transactions | `GET /admin/transactions?search=` | `GET /admin/transactions/{id}` |
| Notifications | `GET /admin/notifications?search=` | `GET /admin/notifications/{id}` |

Payments, transactions, and notifications remain immutable.

## 8. HTTP status guidance

| Status | Meaning |
|---|---|
| 200 | Successful request |
| 201 | Resource created |
| 400 | Invalid request |
| 401 | Missing or invalid authentication |
| 403 | Authenticated but forbidden |
| 404 | Resource not found or not owned |
| 409 | Business conflict |
| 422 | Business validation failure |
| 500 | Unexpected server error |

## 9. V1 exclusions

PayFlow V1 intentionally excludes pagination, refunds, reversals, audit logs, external payment gateways, Kafka, Redis, and microservices.
