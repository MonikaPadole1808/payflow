# PayFlow V1 Architecture Diagram

```mermaid
flowchart TB
    Client[API Client / Postman]
    Security[Spring Security<br/>JWT Filter]
    Controllers[REST Controllers]
    Admin[Admin Orchestration Module]
    Auth[Auth Module]
    User[User Module]
    Wallet[Wallet Module]
    Payment[Payment Module]
    Ledger[Transaction Ledger Module]
    Notification[Notification Module]
    Repositories[Spring Data JPA Repositories]
    DB[(PostgreSQL)]
    Flyway[Flyway V1-V7]

    Client --> Security
    Security --> Controllers

    Controllers --> Auth
    Controllers --> Wallet
    Controllers --> Payment
    Controllers --> Ledger
    Controllers --> Notification
    Controllers --> Admin

    Auth --> User
    Auth --> Wallet
    Auth --> Notification

    Payment --> Wallet
    Payment --> Ledger
    Payment --> Notification

    Wallet --> Ledger
    Wallet --> Notification

    Admin --> User
    Admin --> Wallet
    Admin --> Payment
    Admin --> Ledger
    Admin --> Notification

    Auth --> Repositories
    User --> Repositories
    Wallet --> Repositories
    Payment --> Repositories
    Ledger --> Repositories
    Notification --> Repositories

    Repositories --> DB
    Flyway --> DB
```

## Architectural rules

- PayFlow V1 is a modular monolith.
- Each business table has one owning module.
- Cross-module communication occurs through services, not foreign repositories.
- The Admin module is an orchestration layer and owns no database table.
- Financial operations use transactional boundaries.
- Payments and transaction-ledger records are immutable.
- Wallet status and balance rules remain inside the Wallet module.
