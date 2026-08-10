# Registration PF-500-001 Review Summary

## Original Failing Request And Response

Request:

```http
POST /api/v1/auth/register
Content-Type: application/json
```

Postman body from `postman/PayFlow_Local.postman_environment.json`:

```json
{
  "email": "payflow.user@example.com",
  "password": "Password123"
}
```

Reported response:

```json
{
  "success": false,
  "message": "An unexpected error occurred",
  "errorCode": "PF-500-001",
  "timestamp": "2026-08-10T01:24:52.042268Z"
}
```

Controlled reproduction with invalid JWT secret:

```text
PAYFLOW_JWT_SECRET=secret
POST http://127.0.0.1:18081/api/v1/auth/register
HTTP_STATUS=500
BODY={"success":false,"message":"An unexpected error occurred","errorCode":"PF-500-001","timestamp":"2026-08-10T02:17:36.114404300Z"}
```

## Exact Root Cause And Evidence

The backend accepted an invalid JWT signing secret at startup and failed later when registration attempted to generate the access token.

Confirmed deepest/root exception:

```text
io.jsonwebtoken.security.WeakKeyException
```

Supporting evidence:

- With valid secret `MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=`, PostgreSQL-backed registration returned `201 Created`.
- With duplicate email, PostgreSQL-backed registration returned `409 PF-409-001`.
- With invalid secret `secret`, PostgreSQL-backed registration returned the same `500 PF-500-001` shape as the reported failure.
- After the fix, controlled startup with `PAYFLOW_JWT_SECRET=secret` fails during configuration binding with a JJWT weak-key message before registration traffic can be accepted.

## Registration Flow Point Where It Failed

```text
AuthController.register(...)
  -> AuthService.register(...)
     -> UserAccountService.createActiveUser(...)
     -> WalletProvisioningService.createWalletForUser(...)
     -> NotificationRecorder.recordRegistration(...)
     -> AuthService.createAuthResponse(...)
     -> JwtService.generateAccessToken(...)
     -> JwtService.signingKey(...)
     -> Keys.hmacShaKeyFor(...)
```

Failing class and method:

```text
com.monika.payflow.auth.security.JwtService.signingKey()
```

Why it became `PF-500-001`:

`WeakKeyException` was not a PayFlow business exception, so `GlobalExceptionHandler.handleUnexpectedException(...)` logged it and returned `ErrorCode.INTERNAL_SERVER_ERROR`.

## Files Changed And Why

- `src/main/java/com/monika/payflow/auth/security/JwtProperties.java`: validates JWT secret during configuration binding and fails startup for blank, invalid Base64, or weak HMAC keys.
- `src/test/java/com/monika/payflow/auth/security/JwtServiceTest.java`: adds regression coverage for weak-secret fail-fast behavior.
- `src/test/java/com/monika/payflow/auth/controller/AuthRegistrationIntegrationTest.java`: adds endpoint-level registration coverage for successful response and user/wallet/notification/refresh-token side effects.
- `README.md`: documents local JWT secret requirements.
- `document/Notes/2026-08-10_Registration_PF500_Notes.md`: records root cause, flow point, correction, and verification.
- `document/Q&A/2026-08-10_Registration_PF500_QA.md`: adds interview-style explanation of the failure and fix.
- `document/Notes/Readme.md`, `document/Q&A/Readme.md`, `document/Project_Progress.md`: link and summarize the regression update.

## Description Of The Correction

`JwtProperties` now validates `payflow.security.jwt.secret` as soon as Spring binds configuration. A misconfigured instance fails startup instead of accepting registration requests and failing during JWT generation. The V1 API contract remains unchanged for valid configuration.

## Regression Tests Added

- `JwtServiceTest.weakSecretFailsFastDuringConfigurationBinding`
- `AuthRegistrationIntegrationTest.registerCompletesAllRegistrationSideEffectsAtomically`

## Commands Run With Exact Results

```text
git status --short
Result: clean at start
```

```text
.\gradlew.bat test --tests com.monika.payflow.auth.controller.AuthRegistrationIntegrationTest
Result: BUILD SUCCESSFUL in 19s
```

```text
.\gradlew.bat test --tests com.monika.payflow.auth.security.JwtServiceTest.invalidSecretFailsTokenGeneration
Result before fix: BUILD SUCCESSFUL in 4s, confirming WeakKeyException
```

```text
.\gradlew.bat test --tests com.monika.payflow.auth.security.JwtServiceTest --tests com.monika.payflow.auth.controller.AuthRegistrationIntegrationTest
Result: BUILD SUCCESSFUL in 46s
```

```text
.\gradlew.bat bootRun --args='--server.port=18082'
PAYFLOW_JWT_SECRET=secret
Result after fix: APPLICATION FAILED TO START; failed to bind payflow.security.jwt due WeakKeyException
```

```text
.\gradlew.bat test
Result: BUILD SUCCESSFUL in 34s
```

Repository Gradle tasks do not define separate formatting or static-analysis checks beyond the standard `test` task.

## Manual Postman-Compatible Result

Controlled PostgreSQL-backed app on port `18083` with valid secret:

```text
POST /api/v1/auth/register
email=codex.manual.registration.20260810@example.com
HTTP=201
message=User registered successfully
tokenType=Bearer
```

Login verification:

```text
POST /api/v1/auth/login
HTTP=200
response contained accessToken
```

Duplicate verification:

```text
POST /api/v1/auth/register
same email
HTTP=409
BODY={"success":false,"message":"Email already exists","errorCode":"PF-409-001","timestamp":"2026-08-10T02:23:55.603141600Z"}
```

## Database Verification Result

For `codex.manual.registration.20260810@example.com` after register plus login:

```text
users_count=1
wallets_count=1
registration_notifications_count=1
refresh_tokens_count=2
```

`refresh_tokens_count=2` is expected because registration created one refresh token and login created another.

For failed invalid-secret registration email `codex.invalid.secret.20260810@example.com`:

```text
failed_users_count=0
failed_wallets_count=0
failed_notifications_count=0
failed_refresh_tokens_count=0
```

No partial data remained after the reproduced failure.

## Remaining Risks Or Blockers

- PostgreSQL read-only verification used the cached PostgreSQL JDBC driver because `psql` was not installed.
- The Java compiler emitted a Windows access warning when closing a Gradle-cached driver jar during one read-only DB check, but the SQL query completed and returned the counts above.
- Existing refresh-token behavior creates a new refresh token on each login; this was not changed.

## Git Confirmation

Nothing was staged, committed, pushed, merged, reset, or branched.
