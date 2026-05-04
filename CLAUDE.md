# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

Each service is an independent Gradle project. Run commands from within the service directory.

```bash
./gradlew bootRun          # Run service locally
./gradlew bootJar          # Build fat JAR to build/libs/
./gradlew test             # Run all tests
./gradlew test --tests "fully.qualified.ClassName.methodName"  # Run single test
./gradlew clean bootJar    # Clean rebuild
```

Docker (from a service directory):
```bash
docker compose up --build  # Build image and start service + its DB
docker compose up -d       # Start in background
docker compose down -v     # Stop and remove volumes
```

## Architecture

This is a Spring Boot microservices project. All services use **Java 21** and **Gradle Kotlin DSL**.

### Services & Ports

| Service | Port | Notes |
|---|---|---|
| `eureka-server` | 8761 | Service registry — start first |
| `api-gateway` | 8080 | Routes external traffic |
| `auth-service` | — | Authentication / JWT issuance |
| `order-service` | 8081 | Core business logic, PostgreSQL |
| `inventory-service` | — | Stock management |
| `payment-service` | — | Payment processing |
| `notification-service` | — | Async notifications |

**Startup order:** `eureka-server` → other services (all register themselves on start).

### Inter-Service Communication

Services discover each other via **Eureka** using the `spring.application.name` as the service identifier. Calls are made with **OpenFeign** clients — the `name` in `@FeignClient(name = "inventory-service")` must match the target's `spring.application.name`.

`order-service` calls:
- `inventory-service /api/inventory` — check / reserve / release stock
- `payment-service /api/payments` — process payment

### order-service Architecture

The only fully implemented service. Follows standard layered architecture:

- **Flyway** manages the DB schema (`src/main/resources/db/migration/V{n}__{description}.sql`). Hibernate is set to `ddl-auto: validate` — it never modifies the schema.
- **MapStruct** (`OrderMapper`) generates all entity↔DTO conversions at compile time via annotation processing.
- **JPA Auditing** (`@EnableJpaAuditing`) auto-populates `createdAt`/`updatedAt` via `@CreatedDate`/`@LastModifiedDate` — entities must be annotated with `@EntityListeners(AuditingEntityListener.class)`.
- **`@Transactional(readOnly = true)`** is the class-level default in `OrderServiceImpl`; write methods override with `@Transactional`.
- Security is stateless (`SessionCreationPolicy.STATELESS`). Authentication is delegated to the API Gateway — `order-service` currently permits all requests.

### Adding a New Flyway Migration

Name files strictly as `V{n}__{description}.sql` (two underscores). Flyway runs them in version order on startup. Never edit an already-applied migration file.

### Environment Variables (order-service)

| Variable | Default | Description |
|---|---|---|
| `POSTGRES_ADDRESS` | `localhost` | DB host |
| `POSTGRES_ADDRESS_PORT` | `5433` | DB port |
| `POSTGRES_DB_NAME` | `order_db` | DB name |
| `DATABASE_USERNAME` | `postgres` | |
| `DATABASE_PASSWORD` | `postgres` | |
| `EUREKA_HOST` | `localhost` | Eureka host |
| `EUREKA_PORT` | `8761` | Eureka port |
| `PORT` | `8081` | Server port |