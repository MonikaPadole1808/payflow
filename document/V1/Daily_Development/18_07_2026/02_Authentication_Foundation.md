The project foundation has been completed.

Read the following documents before making any changes.

Priority

1. V1/V1_Architecture_Design_v1.0.md
2. V1/API_Contract.md
3. V1/Database_Design.md
4. Coding_Standards.md
5. Git_Workflow.md
6. Project_Progress.md

The architecture is frozen.

Do not redesign packages or modules.

Implement only the Authentication Foundation.

--------------------------------------------------
Today's Goal
--------------------------------------------------

Implement the authentication infrastructure required for future business features.

Focus on framework setup and authentication flow.

Do not implement Wallet, Payment, Transaction, or Notification modules.

--------------------------------------------------
Tasks
--------------------------------------------------

1. Security Configuration

Configure Spring Security according to the architecture.

Requirements:

- Stateless authentication
- SecurityFilterChain
- PasswordEncoder (BCrypt)
- AuthenticationManager
- Public endpoint configuration
- Protected endpoint configuration
- CORS configuration
- CSRF disabled for REST APIs

--------------------------------------------------

2. JWT Infrastructure

Create reusable JWT components.

Examples:

- JwtService
- JwtAuthenticationFilter
- JwtProperties
- JwtAuthenticationEntryPoint (if required)

JWT should support:

- Token generation
- Token validation
- Claim extraction
- Expiration handling

Do not hardcode secrets.

Read configuration from application properties.

--------------------------------------------------

3. Authentication Package

Review and complete the auth module structure.

Suggested packages:

auth
├── controller
├── dto
├── entity
├── repository
├── service
├── security
├── mapper
└── validator

Create only the classes required for authentication.

--------------------------------------------------

4. Refresh Token Infrastructure

Create the entity and repository required for Refresh Tokens.

Do not implement business logic beyond what is needed for authentication.

--------------------------------------------------

5. User Entity (Minimum Required)

Create the minimum User entity required for authentication.

Include fields such as:

- id
- email
- password
- role
- status
- createdAt
- updatedAt

Do not add profile or business fields yet.

--------------------------------------------------

6. UserDetails Integration

Implement:

- UserDetails
- UserDetailsService

Load users from the database.

--------------------------------------------------

7. Authentication APIs

Implement:

POST /api/v1/auth/register

POST /api/v1/auth/login

POST /api/v1/auth/refresh

Follow the API Contract.

Use standardized ApiResponse.

Use Bean Validation.

--------------------------------------------------

8. Exception Handling

Integrate authentication exceptions with the existing GlobalExceptionHandler.

Return standardized error responses.

--------------------------------------------------

9. Flyway

Create migrations only for:

users

refresh_tokens

Follow Flyway naming conventions.

Do not modify existing migrations.

--------------------------------------------------

10. Testing

Verify:

- Registration
- Login
- Invalid credentials
- Expired token
- Protected endpoint access
- Public endpoint access

Ensure the application starts successfully.

--------------------------------------------------
Do NOT Implement
--------------------------------------------------

Wallet

Payment

Transaction

Notification

Admin

Business logic outside authentication

--------------------------------------------------
Deliverables
--------------------------------------------------

Working Authentication Module

JWT Authentication

Refresh Token Support

Security Configuration

Flyway Migrations

Standardized API Responses

Application builds successfully

No architectural violations

--------------------------------------------------
Before Finishing
--------------------------------------------------

Review every class.

Remove duplicate code.

Use constructor injection.

Follow SOLID principles.

Ensure implementation follows all frozen architecture documents.

Do not modify any architecture documentation.

If any implementation decision conflicts with the architecture documents, stop and report the conflict instead of changing the architecture.