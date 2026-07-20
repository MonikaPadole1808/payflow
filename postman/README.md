# PayFlow V1 Postman Collection

## Files

- `PayFlow_V1.postman_collection.json`
- `PayFlow_Local.postman_environment.json`

## Setup

1. Start PayFlow locally.
2. Import both files into Postman.
3. Select the `PayFlow Local` environment.
4. Confirm `baseUrl` is:

```text
http://localhost:8080/api/v1
```

## Token Handling

Login responses use the standard API envelope:

```json
{
  "success": true,
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "tokenType": "Bearer"
  }
}
```

The collection saves:

- `accessToken` from `data.accessToken`
- `refreshToken` from `data.refreshToken`
- `adminAccessToken` from admin login `data.accessToken`

USER requests use `accessToken`. Admin requests use `adminAccessToken`.

## Recommended USER Flow

1. Authentication / Register
2. Authentication / Login
3. Wallet / Get Wallet
4. Wallet / Deposit
5. Authentication / Register Receiver
6. Authentication / Login Receiver
7. Wallet / Get Receiver Wallet
8. Authentication / Login
9. Payments / Transfer
10. Payments / Payment History
11. Payments / Payment Details
12. Transactions / Transaction History
13. Transactions / Transaction Details
14. Notifications / Notification History
15. Notifications / Notification Details
16. Notifications / Mark As Read

## Admin Setup

The public registration endpoint creates `USER` accounts. To use Admin endpoints, create or promote an admin account before calling admin requests.

Recommended local options:

- Seed an ADMIN user directly in the local database with a BCrypt password hash.
- Temporarily promote a known local user in the database by setting its `role` to `ADMIN`.
- Use an already-existing ADMIN account if your local database has one.

Then set these environment variables:

- `adminEmail`
- `adminPassword`

Run:

1. Authentication / Admin Login
2. Admin Dashboard / Get Dashboard
3. Admin User Management / List/Search Users
4. Admin Wallet Management / List/Search Wallets
5. Admin Payments / List/Search
6. Admin Transactions / List/Search
7. Admin Notifications / List/Search

## Request Payload Notes

Payloads are generated from the implemented DTOs:

- Register: `email`, `password`
- Login: `email`, `password`
- Refresh Token: `refreshToken`
- Wallet Deposit/Withdraw: `amount`
- Payment Transfer: `receiverUserId`, `amount`, `description`
- Admin User Update: `email`, `status`
- Admin Role Update: `role`

Supported enum values:

- User status: `ACTIVE`, `DISABLED`
- User role: `USER`, `ADMIN`

## Troubleshooting

- `400 Bad Request`: validation failed, insufficient balance, blocked wallet, self-transfer, or invalid role/status value.
- `401 Unauthorized`: missing, expired, malformed, or invalid JWT. Run Login again.
- `403 Forbidden`: authenticated user is not ADMIN for `/admin/**`, or security rejected access.
- `404 Not Found`: requested ID does not exist or does not belong to the authenticated user.
- `409 Conflict`: duplicate email or duplicate wallet creation.
- `422 Unprocessable Entity`: reserved by the API contract for business validation; current implementation primarily uses `400` for business validation failures.
- `500 Internal Server Error`: unexpected server-side error. Check application logs and database connectivity.

## Newman

If Newman is installed:

```powershell
newman run postman/PayFlow_V1.postman_collection.json -e postman/PayFlow_Local.postman_environment.json
```

The full collection contains both USER and ADMIN requests. Admin requests require a valid ADMIN account and `adminAccessToken`.
