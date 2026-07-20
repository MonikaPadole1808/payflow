# PayFlow

## Overview

PayFlow is a production-style distributed digital payment processing platform built to demonstrate enterprise backend development using Java and Spring Boot.

The project starts as a modular monolith with clearly defined module boundaries and is designed to evolve into a microservices architecture in future versions.

---

## Technology Stack

Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA

Database

- PostgreSQL
- Flyway

Build Tool

- Gradle

Authentication

- JWT
- Refresh Tokens
- BCrypt

Testing

- JUnit 5
- Mockito

---

## Features

### Authentication

- User Registration
- Login
- JWT Authentication
- Refresh Tokens

### Wallet

- Wallet Creation
- Deposit
- Withdraw
- Wallet Blocking

### Payments

- Wallet-to-Wallet Transfers
- Atomic Processing
- Payment History

### Transaction Ledger

- Immutable Financial Ledger
- Transaction History

### Notifications

- Registration
- Deposit
- Withdrawal
- Payment Sent
- Payment Received

### Administration

- Dashboard
- User Management
- Wallet Management
- Payment Management
- Transaction Management
- Notification Management
- Role Management

---

## Architecture

```
Client
   │
REST Controllers
   │
Service Layer
   │
Business Modules
   │
Repositories
   │
PostgreSQL
```

Modules

- Authentication
- User
- Wallet
- Transaction
- Payment
- Notification
- Admin
- Common

---

## Design Principles

- Modular Monolith
- Clean Architecture
- Layered Architecture
- Repository Ownership
- Service-based Communication
- Immutable Financial Records
- Transactional Consistency

---

## Current Version

Version

V1.0.0

Status

Completed

---

## Future Roadmap

Version 2

- Kafka
- Redis
- Docker
- Monitoring
- Outbox Pattern
- Retry Mechanism
- Microservices
- Kubernetes
- Azure Deployment

---

## License

This project is developed for learning, portfolio, and demonstration purposes.