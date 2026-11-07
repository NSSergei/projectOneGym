# Gym Booking API

Spring Boot REST API for gym users, workouts, training slots, and bookings. The application uses PostgreSQL for durable data, Redis for caching, RabbitMQ for booking events, Prometheus for metrics, and Grafana for dashboards.

## Requirements

- Java 21
- Maven 3.9 or newer
- Docker Compose v2 for the full local stack
- k6 for the load test

## Configuration

Create a `.env` file in the repository root. Keep real credentials out of version control. The Compose stack reads these settings:

| Variable | Purpose |
| --- | --- |
| `JWT_SECRET` | HMAC signing key; use at least 32 random bytes |
| `POSTGRES_URL` | JDBC URL, normally `jdbc:postgresql://postgres:5432/<database>` in Compose |
| `POSTGRES_DB` | PostgreSQL database name |
| `POSTGRES_USER` | PostgreSQL application user |
| `POSTGRES_PASSWORD` | PostgreSQL password |
| `RABBITMQ_USER` | RabbitMQ application user |
| `RABBITMQ_PASSWORD` | RabbitMQ password |
| `ADMIN_EMAIL` | Optional bootstrap administrator email if enabled in a future deployment initializer |
| `ADMIN_PASSWORD` | Optional bootstrap administrator password if enabled in a future deployment initializer |
| `GRAFANA_ADMIN_USER` | Grafana administrator username; defaults to `admin` |
| `GRAFANA_ADMIN_PASSWORD` | Grafana administrator password; change the default before exposing Grafana |
| `APP_PORT` | Host port for the API; defaults to `8080` |
| `POSTGRES_HOST_PORT` | Host port for PostgreSQL; defaults to `5434` |
| `REDIS_HOST_PORT` | Host port for Redis; defaults to `6379` |
| `RABBITMQ_HOST_PORT` | Host port for RabbitMQ; defaults to `5672` |
| `RABBITMQ_MANAGEMENT_HOST_PORT` | Host port for RabbitMQ management; defaults to `15672` |
| `PROMETHEUS_HOST_PORT` | Host port for Prometheus; defaults to `9090` |
| `GRAFANA_HOST_PORT` | Host port for Grafana; defaults to `3000` |

The current Compose file expects a pre-existing Docker volume named `gym_postgres_data` for PostgreSQL. Create it once with `docker volume create gym_postgres_data` before the first startup. This preserves the existing project data volume configuration.

## Run the application stack

```powershell
docker volume create gym_postgres_data
docker compose up --build -d
docker compose ps
```

The API is available at `http://localhost:${APP_PORT:-8080}`. PostgreSQL is published on port `5434`, Redis on `6379`, RabbitMQ on `5672` and its management UI on `15672`. Prometheus is available at `http://localhost:9090`; Grafana is at `http://localhost:3000`.

The initial administrator and coach are inserted by Flyway migrations. Change or replace seeded credentials before using a deployed environment. Do not rely on the sample credentials in a production environment.

## API overview

All API routes except authentication, health, and Prometheus metrics require a bearer token unless a route has a stricter role rule. Register or log in to receive an `access_token`, then send it as `Authorization: Bearer <token>`.

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | Public | Register a client |
| `POST` | `/api/auth/login` | Public | Authenticate and receive a JWT |
| `GET` | `/api/users` | Authenticated | List users |
| `GET` | `/api/users/{id}` | Authenticated | Get one user |
| `GET` | `/api/training` | Administrator | List training slots |
| `GET` | `/api/training/{id}` | Administrator | Get a training slot |
| `POST`, `PUT`, `DELETE` | `/api/training` | Administrator | Manage training slots |
| `GET` | `/api/workout` | Administrator | List workouts |
| `POST`, `PUT`, `DELETE` | `/api/workout` | Administrator | Manage workouts |
| `POST` | `/api/booking/registration/{trainingSlotId}` | Client | Book a training slot |
| `PATCH` | `/api/booking/{trainingSlotId}/cancel` | Authenticated | Cancel the current user's booking |
| `GET` | `/actuator/health` | Public | Liveness and readiness health |
| `GET` | `/actuator/prometheus` | Public | Prometheus scrape endpoint |

Interactive API documentation is available at `http://localhost:8080/swagger-ui.html`; its access is restricted to administrators. OpenAPI JSON is served at `/docs`.

## Tests

Run all unit and integration tests with:

```powershell
mvn verify
```

Unit tests cover authentication, JWT validation, user and workout services, training-slot rules and mappings, and booking lifecycle behavior. The Spring Boot integration test exercises registration, password persistence, login, JWT authentication, email validation, and access to a protected API resource using an in-memory H2 database. It does not require PostgreSQL, Redis, or RabbitMQ.

The current suite contains 53 tests. The latest clean run passed all 53 with no failures or skips and reported 88.26% line coverage, 85.71% branch coverage, and 88.8% instruction coverage. JaCoCo generates an HTML report at `target/site/jacoco/index.html`; `mvn verify` enforces a minimum 80% line coverage for the full bundle.

Run the API end-to-end authentication flow against a running application with:

```powershell
k6 run e2e-tests/auth-flow.js
```

The end-to-end script creates a unique client, logs in, then verifies the JWT against a protected user-list request.

## Load testing

Start the Compose stack, install k6, then run:

```powershell
k6 run load-tests/gym.js
```

The script creates a client during setup, then ramps through 10, 25, and 50 virtual users issuing health checks and authenticated user-list requests. It enforces an error rate below 2%, p95 below 750 ms, and p99 below 1.5 seconds. Override the target with `BASE_URL`, for example:

```powershell
$env:BASE_URL = 'http://localhost:8080'
k6 run load-tests/gym.js
```

This is an initial baseline workload rather than a capacity certification. It does not create bookings or load-test write operations, and its thresholds are starting SLOs. Increase the duration and VU stages gradually, include representative authenticated write traffic, and monitor database pool saturation and RabbitMQ queue depth before drawing capacity conclusions.

### Recorded local run

The documented scenario was run on 2026-11-07 against the final local Docker Compose stack with 50 peak virtual users for three minutes. It issued 8,317 HTTP requests across 4,158 iterations, with zero failed requests. Observed HTTP latency was 2.66 ms average, 2.29 ms median, 4.67 ms p95, and 331.15 ms maximum. The measured request rate was 46.0 requests per second across the health and authenticated user-list routes.

These results are a local smoke-load baseline. The run used a small local database and did not set CPU or memory limits for the containers. It does not establish production capacity, and it does not load booking writes, queue backlog behavior, or production-sized data. Repeat with resource limits and representative data before capacity planning.

## Metrics and dashboards

Spring Boot Actuator and Micrometer expose JVM, process, HTTP server, datasource pool, cache, and application health metrics. Booking counters track created, cancelled, and rejected bookings with low-cardinality dimensions. Prometheus scrapes the application every 15 seconds from `observability/prometheus/prometheus.yml`. PostgreSQL and Redis exporters expose database and cache metrics; RabbitMQ's Prometheus plugin exposes broker and per-queue metrics. Grafana provisions the Prometheus datasource and the `Gym API Overview` dashboard automatically.

The dashboard tracks HTTP request rate, p95 request latency, 5xx ratio, active HikariCP connections, PostgreSQL sessions, Redis memory, RabbitMQ queue depth, exporter health, and booking activity. Prometheus data and Grafana state use named Docker volumes. Metrics endpoints contain operational details; restrict network access to service ports in deployed environments or place metrics behind an internal network. RabbitMQ per-queue metrics are enabled for this small set of queues; review metric cardinality before using the same setting on a broker with many queues.

## Current fixes and optimization backlog

The following issues were found while reviewing the project and addressed in this change:

- Booking registration tried to use an `exists...` repository query as an existing booking, then mutated it instead of creating a new booking. Registration now checks for a duplicate, creates a booking, and rejects a full slot.
- Concurrent bookings could exceed slot capacity. Registration now uses a transaction and locks the training slot while checking capacity and saving.
- Request DTO validation used string-only `@NotBlank` constraints on numeric and date values. These constraints were corrected, and registration validates email format.
- Repository interfaces redeclared `findById` with primitive IDs, creating overloads beside Spring Data's inherited `Long` lookup. The duplicate signatures and unused booking query declarations were removed.
- Two separate RabbitMQ listeners competed for the same booking queues and printed full event payloads. The duplicate listener was removed and notifications now use structured logs without logging cancellation reasons.
- Authentication debug output included request and credential-adjacent data in logs. Controller and service `System.out` diagnostics were removed.
- SQL statements were printed for every request. Hibernate SQL output is disabled and Open Session in View is disabled.
- JWT unit test had conflicting Mockito and Reactor `when` imports and a masked signing key. It now uses Mockito only and a valid test key.
- Spring dependencies included duplicate test declarations. The duplicate entries were removed.
- Prometheus scraping, Grafana provisioning, readiness checks, and dependency health checks were missing. These are now configured in the Compose stack.
- API not-found and request validation errors now use consistent HTTP Problem Details responses.

Remaining work to prioritize before production:

1. Implement a transactional outbox for booking events. Database commits and RabbitMQ publishing are currently separate operations, so a broker failure after a database commit can leave an event unpublished.
2. Add pagination and bounded query results to user, workout, booking, and training list endpoints.
3. Replace entity-based request and response bodies with dedicated DTOs, especially for user updates, to prevent changing roles, balances, or password hashes through mass assignment.
4. Review authorization ownership rules for booking read, update, delete, and cancellation routes. Several booking operations currently allow any authenticated principal or rely on role-level checks only.
5. Normalize API paths and request formats, return consistent `201 Created` and `204 No Content` responses, and add API versioning before clients depend on the current routes.
6. Replace seeded administrator and coach credentials with a secure bootstrap mechanism and remove credentials from migrations.
7. Add database-level check constraints for slot capacity and time ranges, and decide whether cancelled bookings can be recreated or reactivated.
8. Add distributed tracing, alert rules, and delivery success/failure metrics for each messaging workflow.
9. Run authenticated read/write load tests and concurrency tests against PostgreSQL and RabbitMQ, then tune connection pools, indexes, JVM limits, and rate limits from measured results.
10. Add CI validation, dependency vulnerability scanning, secret scanning, and a production deployment profile with network restrictions for management endpoints.

