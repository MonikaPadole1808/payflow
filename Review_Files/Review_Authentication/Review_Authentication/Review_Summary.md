# Authentication Foundation Review Summary

## Authentication Implementation Overview

PayFlow V1 now contains the authentication foundation for a modular monolith payment platform. The implementation provides user registration, login, JWT access-token generation, refresh-token persistence, stateless request authentication, and standardized API/error responses.

The implementation intentionally stops at authentication infrastructure. Wallet, payment, transaction, notification, admin, and business workflows are not included in this milestone.

## Project Package Structure

Primary source package:

```text
com.monika.payflow
├── auth
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   ├── security
│   └── service
├── common
│   ├── api
│   ├── config
│   ├── constants
│   ├── error
│   └── exception
└── user
    ├── entity
    ├── repository
    └── service
```

## Modules Implemented

- `auth`: Authentication controller, DTOs, refresh-token entity/repository, authentication service, JWT support, Spring Security user details integration.
- `user`: Minimum authentication user model, user repository, and `UserAccountService` boundary used by authentication.
- `common`: Standard response wrapper, error-code enum, global exception handling, application constants, and security configuration.

## Flyway Migrations Added

- `V1__init.sql`: Enables PostgreSQL `pgcrypto` extension.
- `V2__create_users_table.sql`: Creates `users` with UUID primary key, email uniqueness, password hash, role, status, and timestamps.
- `V3__create_refresh_tokens_table.sql`: Creates `refresh_tokens` with UUID primary key, user foreign key, unique token, expiration timestamp, and created timestamp.

## Security Configuration Summary

- Stateless Spring Security configuration.
- CSRF disabled for REST APIs.
- CORS configured from environment-backed application configuration.
- BCrypt password encoder.
- `AuthenticationManager` exposed through Spring authentication configuration.
- DAO authentication provider using database-backed `UserDetailsService`.
- Public endpoints:
  - `/api/v1/auth/register`
  - `/api/v1/auth/login`
  - `/api/v1/auth/refresh`
  - `/actuator/health`
- All other endpoints require authentication.
- Standard JSON response for unauthorized requests through `JwtAuthenticationEntryPoint`.

## JWT Flow Summary

1. User registers or logs in.
2. Credentials are validated.
3. Access token is generated using configured JWT secret and expiration.
4. Refresh token is generated as a random UUID string and stored in PostgreSQL.
5. Protected API requests send `Authorization: Bearer <token>`.
6. `JwtAuthenticationFilter` extracts and validates the token.
7. Valid token sets authentication in the Spring Security context.
8. Refresh endpoint validates refresh-token existence and expiration, then issues a new token pair.

## Authentication Endpoints

- `POST /api/v1/auth/register`
  - Request: email, password
  - Response: access token, refresh token, token type
- `POST /api/v1/auth/login`
  - Request: email, password
  - Response: access token, refresh token, token type
- `POST /api/v1/auth/refresh`
  - Request: refresh token
  - Response: access token, refresh token, token type

All endpoints return `ApiResponse`.

## Test Summary

Authentication-related tests included:

- `AuthServiceTest`
  - Registration creates a user with encoded password and returns tokens.
  - Duplicate email registration is rejected.
  - Invalid login credentials are rejected.
  - Expired refresh token is rejected.
- `JwtServiceTest`
  - Generated token validates for the same user.
  - Expired token is rejected.

Last verification command:

```text
.\gradlew.bat test
```

Result:

```text
BUILD SUCCESSFUL
```

## Known Limitations

- Refresh tokens are persisted but not rotated/revoked beyond issuing a new token pair.
- Logout endpoint is not implemented.
- Token revocation list is not implemented.
- Role-based authorization exists at authority level but no role-specific business endpoints are implemented yet.
- Swagger/OpenAPI documentation is not yet generated.
- Production-grade secret management is represented through environment variables, not a vault.
- Integration tests with real PostgreSQL/Testcontainers are not yet added.
- User profile/business fields are intentionally excluded.

## Runtime Requirements

- Java 21
- PostgreSQL database
- Flyway migrations enabled
- Environment variable `PAYFLOW_JWT_SECRET`
- Optional environment variables for token durations and CORS origins

## External Dependencies

- Spring Boot Web
- Spring Boot Data JPA
- Spring Boot Security
- Spring Boot Validation
- Spring Boot Actuator
- PostgreSQL JDBC Driver
- Flyway Core
- Flyway PostgreSQL support
- JJWT `0.12.6`
- JUnit 5 / Spring Boot Test
- Spring Security Test
- H2 test runtime

## Environment Variables Required

Required:

- `PAYFLOW_JWT_SECRET`: Base64 encoded JWT signing secret.

Optional:

- `PAYFLOW_JWT_ACCESS_TOKEN_EXPIRATION_MINUTES`: Defaults to `15`.
- `PAYFLOW_JWT_REFRESH_TOKEN_EXPIRATION_DAYS`: Defaults to `7`.
- `PAYFLOW_CORS_ALLOWED_ORIGINS`: Defaults to `http://localhost:3000`.
