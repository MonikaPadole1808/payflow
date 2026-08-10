# Registration PF-500-001 Diagnosis

Date: 10-Aug-2026

Endpoint:

```http
POST /api/v1/auth/register
```

## Confirmed Root Cause

Registration returned `PF-500-001` when the application was started with an invalid JWT signing secret, such as:

```text
PAYFLOW_JWT_SECRET=secret
```

The registration transaction reached access-token creation after creating the user, wallet, and registration notification in the current persistence context. `JwtService.generateAccessToken(...)` then attempted to build an HMAC signing key from `JwtProperties.secret()`.

JJWT rejected the decoded key as too weak:

```text
io.jsonwebtoken.security.WeakKeyException
```

Because this was an unhandled runtime exception, `GlobalExceptionHandler.handleUnexpectedException(...)` converted it to the generic internal error response:

```json
{
  "success": false,
  "message": "An unexpected error occurred",
  "errorCode": "PF-500-001"
}
```

## Registration Flow Point

Failure point:

```text
AuthController.register(...)
  -> AuthService.register(...)
     -> UserAccountService.createActiveUser(...)
     -> WalletProvisioningService.createWalletForUser(...)
     -> NotificationRecorder.recordRegistration(...)
     -> AuthService.createAuthResponse(...)
     -> JwtService.generateAccessToken(...)
     -> JwtService.signingKey(...)
```

## Correction

`JwtProperties` now validates `payflow.security.jwt.secret` during configuration binding.

The application fails startup if the secret is blank, not valid Base64, or decodes to an HMAC key shorter than 256 bits. This prevents a misconfigured server from accepting registration traffic and failing mid-request.

The API contract remains unchanged. Valid registration still returns `201 Created`; duplicate email still returns `409 PF-409-001`; unexpected runtime failures still use the generic internal error response without exposing internals.

## Atomicity

`AuthService.register(...)` remains `@Transactional`. When token creation failed before the fix, the runtime exception marked the registration transaction for rollback, so no partial user, wallet, notification, or refresh-token records should remain from that failed request.

## Verification Notes

- Reproduced `500 PF-500-001` by running the backend with `PAYFLOW_JWT_SECRET=secret` and calling registration.
- Confirmed valid secret registration succeeds against PostgreSQL on a controlled local port.
- Confirmed duplicate registration returns `409 PF-409-001`.
- Confirmed bad secret now fails during startup configuration binding instead of during registration.
