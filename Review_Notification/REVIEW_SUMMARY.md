# Notification Module Review Summary

## Files Changed

- Added notification module under `src/main/java/com/monika/payflow/notification`.
- Added notification tests under `src/test/java/com/monika/payflow/notification`.
- Added Flyway migration `V7__create_notifications_table.sql`.
- Updated `AuthService` to record registration notifications.
- Updated `WalletService` to record deposit and withdrawal notifications.
- Updated `PaymentService` to record payment sent and payment received notifications.
- Updated existing auth, wallet, payment, and repository tests for notification integration and deterministic ordering.
- Updated `API_Contract.md` and `Database_Design.md`.

## APIs Added or Modified

- Added `GET /api/v1/notifications`.
- Added `GET /api/v1/notifications/{id}`.
- Added `PATCH /api/v1/notifications/{id}/read`.
- No public notification creation API was added.

## Database Changes

- Added `notifications` table.
- Added required notification fields:
  - `id`
  - `user_id`
  - `notification_type`
  - `status`
  - `title`
  - `message`
  - `reference_number`
  - `created_at`
  - `read_at`
- Added foreign key `fk_notifications_user`.
- Added check constraints for supported types, statuses, read state, and non-blank text fields.
- Added index `idx_notifications_user_created_at`.

## Business Rules Implemented

- Notifications are created internally only.
- Registration creates a `REGISTRATION` notification.
- Successful wallet deposit creates a `DEPOSIT` notification.
- Successful wallet withdrawal creates a `WITHDRAW` notification.
- Successful payment transfer creates one `PAYMENT_SENT` notification for the sender.
- Successful payment transfer creates one `PAYMENT_RECEIVED` notification for the receiver.
- Users can retrieve only their own notifications.
- Mark-as-read is owner-scoped and idempotent.
- Notifications are never deleted by the API.

## Tests Added or Updated

- Added `NotificationServiceTest`.
- Added `NotificationRepositoryTest`.
- Added `NotificationControllerTest`.
- Updated `AuthServiceTest`.
- Updated `WalletServiceTest`.
- Updated `PaymentServiceTest`.
- Updated payment and transaction repository ordering tests to avoid timestamp tie flakiness.

## Documentation Updated

- `document/V1/Architecture/API_Contract.md`
- `document/V1/Architecture/Database_Design.md`

Per request, `Project_Progress.md`, Notes, and Q&A were not updated by this implementation.

## Verification Results

- `.\gradlew.bat test` passed.
- Application startup was verified successfully.
- Flyway validated seven migrations and applied `V7__create_notifications_table.sql`.
- JPA mappings initialized successfully.
- Tomcat started on random port `57437`.
- The final `bootRun` task ended as failed only because the verified Java process was intentionally stopped after startup.

## Known Limitations

- Version 1 stores notification history only.
- Email, SMS, push notifications, Kafka, schedulers, retries, and outbox pattern are intentionally out of scope.
- Notification history is not paginated yet.
- No admin notification APIs are implemented.
- Existing workspace changes to `Project_Progress.md` and daily-development file moves were left untouched and excluded from this review patch.
