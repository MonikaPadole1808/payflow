# CLAUDE.md

Version: 1.0
Project: PayFlow
Author: Monika
Purpose: Project Engineering Standards

---

# 1. Project Vision

PayFlow is a production-grade backend payment processing platform built to demonstrate senior-level backend engineering skills.

The project is intended to showcase clean architecture, maintainability, scalability, testing, security, and production-ready engineering practices.

This project is not a tutorial or CRUD application.

Every implementation should reflect how a real engineering team would build and maintain a payment platform.

---

# 2. Long-Term Goals

Primary Goals

- Build a production-quality backend system.
- Demonstrate strong Java and Spring Boot expertise.
- Learn distributed system design.
- Maintain clean Git history.
- Prepare for product-based company interviews.

Future Goals

- Migrate selected modules into microservices.
- Deploy on Azure.
- Add CI/CD.
- Add observability.
- Add monitoring.
- Support high scalability.

---

# 3. Architecture

Current Architecture

Modular Monolith

Future Architecture

Microservices

Current modules should be designed in a way that allows extraction into independent microservices later with minimal changes.

---

# 4. Technology Stack

Language

- Java 21

Framework

- Spring Boot 3.x

Build Tool

- Gradle

Database

- PostgreSQL

Database Migration

- Flyway

Caching

- Redis (future)

Messaging

- Kafka (future)

Containerization

- Docker

Cloud

- Azure (future)

Documentation

- OpenAPI / Swagger

Testing

- JUnit 5
- Mockito
- Testcontainers (later)

---

# 5. Package Structure

Feature-based packaging only.

Example

com.monika.payflow

- common
- auth
- user
- wallet
- payment
- transaction
- notification
- configuration

Do not organize packages by Controller, Service, Repository globally.

Every feature owns its own package.

---

# 6. Coding Standards

Use

- Constructor Injection only.
- Immutable objects whenever possible.
- Java Records for DTOs where appropriate.
- Meaningful class and method names.
- Small focused methods.
- Single Responsibility Principle.

Avoid

- Field Injection.
- Static mutable state.
- God classes.
- Utility classes with business logic.
- Duplicate code.

---

# 7. SOLID Principles

Every feature must follow SOLID principles.

Prefer composition over inheritance.

Depend on abstractions rather than implementations.

---

# 8. Layer Responsibilities

Controller

- Request validation
- HTTP mapping
- Response generation

Service

- Business logic only

Repository

- Database access only

Entity

- Persistence model only

DTO

- API contract only

Business logic must never exist inside Controllers.

---

# 9. Database Standards

Use PostgreSQL.

Never modify schema manually.

Every database change must be managed through Flyway migration scripts.

Never use "ddl-auto=create" or "ddl-auto=update" in production.

Use meaningful table names.

Prefer UUID primary keys where appropriate.

---

# 10. Exception Handling

Use Global Exception Handler.

Create custom exceptions where required.

Never expose stack traces through REST APIs.

Return consistent error responses.

---

# 11. Logging

Use SLF4J.

Log

- Important business events
- Validation failures
- Exceptions
- Startup information

Never log

- Passwords
- Secrets
- JWT tokens
- API keys

---

# 12. API Standards

RESTful APIs only.

Use

GET

POST

PUT

PATCH

DELETE

Follow proper HTTP status codes.

Version APIs when required.

---

# 13. Validation

Use Bean Validation.

Validate all incoming requests.

Never trust client input.

---

# 14. Security

Follow secure coding practices.

Use JWT Authentication.

Never hardcode credentials.

Store secrets using environment variables.

---

# 15. Testing Standards

Every business service should have unit tests.

Critical workflows should have integration tests.

Avoid unnecessary mocking.

Tests should be readable and deterministic.

---

# 16. Git Workflow

Branch naming

feature/<feature-name>

bugfix/<bug-name>

hotfix/<bug-name>

Use Pull Request style development even if working alone.

Keep commits small and meaningful.

---

# 17. Commit Convention

Examples

feat(auth): implement JWT authentication

fix(wallet): correct balance calculation

refactor(payment): simplify payment service

test(user): add service unit tests

docs(readme): update setup guide

---

# 18. Code Quality

Code should always be

- Readable
- Maintainable
- Testable
- Production-ready

Prefer clarity over cleverness.

---

# 19. AI Assistant Rules

When generating code:

Always

- Follow existing architecture.
- Keep changes minimal.
- Explain significant design decisions.
- Generate production-quality code.
- Maintain consistency with existing modules.

Never

- Rewrite unrelated code.
- Introduce unnecessary dependencies.
- Ignore existing conventions.
- Add code without explanation when architecture changes.

---

# 20. Definition of Done

A feature is complete only if

- Code compiles.
- Tests pass.
- API works.
- Database migration exists if required.
- Logging is added.
- Validation is implemented.
- Exceptions are handled.
- Documentation is updated.
- Code follows project standards.

---

# 21. Future Roadmap

Phase 1

- Modular Monolith

Phase 2

- Redis
- Kafka
- Docker Compose

Phase 3

- Authentication
- Authorization
- Observability
- Metrics

Phase 4

- Microservices migration

Phase 5

- Kubernetes
- Azure Deployment
- CI/CD
- Performance optimization

---

End of Document