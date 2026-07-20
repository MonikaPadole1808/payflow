# Wallet Module Review Summary

## Files Changed

Source:

- `src/main/java/com/monika/payflow/wallet/controller/WalletController.java`
- `src/main/java/com/monika/payflow/wallet/dto/WalletAmountRequest.java`
- `src/main/java/com/monika/payflow/wallet/dto/WalletResponse.java`
- `src/main/java/com/monika/payflow/wallet/entity/Wallet.java`
- `src/main/java/com/monika/payflow/wallet/entity/WalletStatus.java`
- `src/main/java/com/monika/payflow/wallet/repository/WalletRepository.java`
- `src/main/java/com/monika/payflow/wallet/service/WalletProvisioningService.java`
- `src/main/java/com/monika/payflow/wallet/service/WalletService.java`
- `src/main/java/com/monika/payflow/auth/service/AuthService.java`
- `src/main/java/com/monika/payflow/common/error/ErrorCode.java`
- `src/main/java/com/monika/payflow/common/exception/BadRequestException.java`
- `src/main/java/com/monika/payflow/common/exception/ConflictException.java`
- `src/main/java/com/monika/payflow/common/exception/ResourceNotFoundException.java`

Tests:

- `src/test/java/com/monika/payflow/wallet/service/WalletServiceTest.java`
- `src/test/java/com/monika/payflow/wallet/repository/WalletRepositoryTest.java`
- `src/test/java/com/monika/payflow/auth/service/AuthServiceTest.java`

Configuration and database:

- `src/main/resources/db/migration/V4__create_wallets_table.sql`

Documentation:

- `document/V1/Architecture/API_Contract.md`
- `document/V1/Architecture/Database_Design.md`
- `document/Project_Progress.md`

## APIs Added

- `GET /api/v1/wallet`
  - Returns the authenticated user's wallet.
- `POST /api/v1/wallet/deposit`
  - Deposits a positive amount into the authenticated user's wallet.
- `POST /api/v1/wallet/withdraw`
  - Withdraws a positive amount from the authenticated user's wallet.

All Wallet APIs are protected and return standardized `ApiResponse` responses.

## Database Changes

Added Flyway migration:

- `V4__create_wallets_table.sql`

Table:

- `wallets`

Important constraints:

- UUID primary key.
- `user_id` foreign key to `users(id)`.
- Unique `user_id` to enforce one wallet per user.
- `balance >= 0` check constraint.
- 3-character currency check.
- status check for `ACTIVE` and `BLOCKED`.

## Tests Added

- Wallet service unit tests for:
  - Automatic wallet creation.
  - Duplicate wallet rejection.
  - Balance inquiry.
  - Missing wallet rejection.
  - Deposit.
  - Withdraw.
  - Insufficient balance rejection.
  - Service-level invalid amount rejection.
- Wallet repository slice tests for:
  - Find wallet by user id.
  - Existence check by user id.
- Authentication service test updated to verify wallet creation after registration.

## Documentation Updated

- API contract updated with wallet request/response details.
- Database design updated with implemented `wallets` table details.
- Project progress updated with Wallet milestone status.

## Verification

Tests:

```text
.\gradlew.bat test
BUILD SUCCESSFUL
```

Application startup:

```text
.\gradlew.bat bootRun --args='--server.port=0'
Started PayflowApplication
Successfully applied migrations up to v4
```

Note: `bootRun` was manually stopped after successful startup verification.

## Known Limitations

- Wallet supports only default `INR` currency in V1.
- Deposit and withdraw update wallet balance only; transaction history is not implemented yet.
- Payment, transfer, notification, and transaction modules are intentionally excluded.
- No admin wallet APIs are implemented.
- Repository integration tests use H2 with Flyway disabled; PostgreSQL migration compatibility was verified through application startup against local PostgreSQL.

## Patch File

- `Review_Wallet/wallet-module.patch`
