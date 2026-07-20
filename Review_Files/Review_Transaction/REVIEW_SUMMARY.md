# Transaction Ledger Review Summary

## Files Changed

- Added transaction module under `src/main/java/com/monika/payflow/transaction`.
- Added transaction tests under `src/test/java/com/monika/payflow/transaction`.
- Updated wallet deposit and withdraw logic in `WalletService`.
- Updated wallet service tests to verify transaction recording.
- Added Flyway migration `V5__create_transactions_table.sql`.
- Updated `API_Contract.md`, `Database_Design.md`, and `Project_Progress.md`.

## APIs Added

- `GET /api/v1/transactions`
  - Returns authenticated user's transaction history.
  - Orders transactions newest to oldest.
- `GET /api/v1/transactions/{id}`
  - Returns one transaction owned by the authenticated user.

No create transaction API was added. Transaction creation is internal only.

## Database Changes

- Added `transactions` table.
- Added required ledger columns:
  - `id`
  - `wallet_id`
  - `transaction_type`
  - `status`
  - `amount`
  - `balance_before`
  - `balance_after`
  - `currency`
  - `reference_number`
  - `description`
  - `created_at`
- Added foreign key to `wallets(id)`.
- Added unique constraint on `reference_number`.
- Added check constraints for supported type, status, positive amount, non-negative balances, and currency length.
- Added index `idx_transactions_wallet_created_at`.

## Business Rules Implemented

- Every successful wallet deposit records exactly one `DEPOSIT` transaction.
- Every successful wallet withdrawal records exactly one `WITHDRAW` transaction.
- Transactions are saved with `SUCCESS` status.
- Balance snapshots are captured before and after wallet mutation.
- Transaction amounts must be positive and have at most two decimal places.
- Users can read only their own transactions.
- Transactions are immutable from the application API perspective.
- Transactions are never exposed through a public creation endpoint.

## Tests Added

- `TransactionServiceTest`
  - Deposit recording.
  - Withdrawal recording.
  - Invalid amount rejection.
  - Transaction history mapping.
  - Owner-scoped transaction lookup.
- `TransactionRepositoryTest`
  - User-scoped transaction history ordered newest first.
  - Cross-user transaction lookup rejection.
- `TransactionControllerTest`
  - Standardized API response for history.
  - Standardized API response for details.
- Updated `WalletServiceTest`
  - Deposit records a transaction.
  - Withdrawal records a transaction.
  - Failed withdrawal does not record a transaction.
  - Invalid deposit does not record a transaction.

## Documentation Updated

- `document/V1/Architecture/API_Contract.md`
- `document/V1/Architecture/Database_Design.md`
- `document/Project_Progress.md`

The frozen architecture design document was not modified.

## Verification

- `.\gradlew.bat test` passed.
- Application startup was verified with Spring Boot.
- Flyway validated five migrations and applied `V5__create_transactions_table.sql` successfully.

## Known Limitations

- Transaction history is not paginated yet.
- Only `DEPOSIT` and `WITHDRAW` transaction types are implemented.
- Only `SUCCESS` transaction status is implemented.
- Payment, transfer, refund, notification, scheduler, and admin transaction APIs remain out of scope for this milestone.
