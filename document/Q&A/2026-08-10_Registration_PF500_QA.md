# Registration PF-500-001 Q&A

## What caused registration to return PF-500-001?

The backend was running with an invalid JWT secret. The value `secret` is not a secure 256-bit HMAC key after Base64 decoding. Registration failed when `JwtService` tried to create the access token.

## Why did this happen after user, wallet, and notification work began?

`AuthService.register(...)` creates the user, provisions the wallet, records the registration notification, and then creates the authentication response. Access-token creation happens in `createAuthResponse(...)`, near the end of the registration flow.

## What was the exact exception?

The deepest confirmed exception was:

```text
io.jsonwebtoken.security.WeakKeyException
```

## Why did the client receive PF-500-001?

No PayFlow business exception handled this configuration failure. It reached `GlobalExceptionHandler.handleUnexpectedException(...)`, which correctly returned the generic internal server error response without exposing stack trace or JWT details.

## Did duplicate email cause this failure?

No. Duplicate email was verified separately and returns:

```http
409 Conflict
```

with error code:

```text
PF-409-001
```

## What changed?

`JwtProperties` now validates `payflow.security.jwt.secret` during Spring configuration binding. If the secret is blank, not Base64, or too weak for HMAC signing, application startup fails.

## Why is fail-fast better than mapping this to a client error?

An invalid JWT secret is server misconfiguration, not a bad registration request. The correct production behavior is to reject startup so the service does not accept traffic in a broken security state.

## Does registration remain atomic?

Yes. Registration still runs inside `AuthService.register(...)` with `@Transactional`. Runtime failures roll back the transaction, so user, wallet, notification, and refresh-token data are not partially committed.

## What should developers use for local startup?

Use a Base64-encoded key with at least 32 bytes of decoded entropy. Example for local development only:

```text
PAYFLOW_JWT_SECRET=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=
```
