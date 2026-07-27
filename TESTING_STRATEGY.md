# Testing Strategy

## Project Overview

This project is a Java 21 / Spring Boot microservices system with five services:
- **eureka** – service registry
- **gateway** – API gateway (Spring Cloud Gateway MVC)
- **resource-service** – MP3 upload, S3 storage, PostgreSQL, RabbitMQ producer
- **resource-processor** – RabbitMQ consumer, Apache Tika metadata extraction
- **song-service** – song metadata CRUD, PostgreSQL

The upload flow is:
```
Client → gateway → resource-service (S3 + DB + RabbitMQ) → resource-processor (Tika) → song-service
```

## 1. Unit Tests

**Framework:** JUnit 5 + Mockito + AssertJ

**Selected module:** `resource-service`

This module is chosen because it contains the most business-critical and testable logic in isolation: upload validation, S3 key generation, delete orchestration, and RabbitMQ event publishing. All external collaborators (S3Client, ResourceRepository, RabbitTemplate) are mockable without containers.

**What is tested:**

| Class | Scenarios covered |
|---|---|
| `ResourceServiceImpl` | Successful upload saves entity and publishes event; download returns bytes; delete removes S3 object and DB record; delete notifies song-service |
| Custom validators (`@AudioContent`) | Valid MP3 passes; non-MP3 content rejected with 400 |
| Exception handling | `ResourceNotFoundException` on unknown ID; partial delete returns IDs of deleted items only |

**Coverage target:** 80%+ line coverage on `resource-service` service and domain classes, enforced by JaCoCo.

---

## 2. Integration Tests

**Framework:** Spock (Groovy) + Testcontainers + WireMock

Spock's Groovy DSL makes integration tests more readable than plain JUnit, and its built-in `given/when/then` blocks map directly to the integration story being told. `@Unroll` and data tables make multi-case adapter tests concise.

**Scope (per-service adapter slices):**

| Service | Slice | What is verified |
|---|---|---|
| `resource-service` | JPA repository | URL persistence, ID generation, bulk delete by IDs |
| `resource-service` | S3 adapter | Upload bytes, download bytes, delete object against LocalStack |
| `resource-service` | RabbitMQ producer | Message published to `resource.queue` with correct `resourceId` payload |
| `resource-processor` | RabbitMQ consumer | Incoming message triggers Tika extraction; HTTP call to song-service is made |
| `song-service` | JPA repository | CRUD queries, unique-title constraint, pagination |



**Coverage target:** All repository methods and all external-adapter classes (S3, RabbitMQ producer/consumer) covered.

---

## 3. Component Tests

**Framework:** Cucumber (Gherkin feature files) + Spring Boot Test + Testcontainers + WireMock

Component tests describe the behaviour of a single service at the business level. Gherkin feature files serve as living documentation that non-technical stakeholders can read. Each service has its own `src/test/resources/features/` directory. Downstream services are stubbed with WireMock so the test scope stays within one service boundary.

**Scope:**

| Service | Stub dependencies |
|---|---|
| `resource-service` | song-service (WireMock) |
| `resource-processor` | resource-service + song-service (WireMock) |
| `song-service` | none (self-contained) |

**Coverage target:** All public API endpoints and all RabbitMQ listener paths covered by at least one scenario.

---

## 4. Contract Tests

**Framework:** Pact JVM (`pact-jvm-consumer`, `pact-jvm-provider`) + Pact Broker

Contract tests protect independently deployed services from schema drift. Both communication styles used in this project are covered:
- **Synchronous HTTP** – resource-processor → resource-service and resource-processor → song-service
- **Asynchronous messaging** – resource-service → resource-processor (RabbitMQ)

Pact files (JSON) are published to a **Pact Broker** after consumer tests pass. Provider verification runs in each provider service's CI pipeline, pulling the pact file from the broker and verifying it against a live application instance started with Testcontainers. The broker also serves as a **stub registry**: the generated pact file can be replayed by `pact-jvm-server` or imported into WireMock, eliminating the need to hand-write WireMock stubs for component tests.

### Contracts table

| Consumer | Provider | Style | Contract covers |
|---|---|---|---|
| `resource-processor` | `resource-service` | HTTP GET | Fetch MP3 bytes for a known ID; 404 shape for unknown ID |
| `resource-processor` | `song-service` | HTTP POST | Create song metadata request/response body |
| `resource-processor` | `resource-service` | AMQP message | Payload shape of `resource.queue` message |

### Consumer side – HTTP contract (resource-processor → song-service)

**Coverage target:** 100% of cross-service HTTP endpoints and all messaging interfaces covered by at least one Pact interaction.

---

## 5. End-to-End Tests

**Framework:** Cucumber (shared with component tests) + Testcontainers `DockerComposeContainer` + RestAssured + Awaitility

E2E tests are written in the same Gherkin format as component tests, but they run against the full stack (all five services via `compose-e2e.yaml`) and speak only to the gateway. Feature files live in a dedicated `e2e-tests/` Maven module. Scenarios focus exclusively on the API layer — they assert HTTP status codes, response bodies, and observable side-effects (metadata populated, resource gone after delete). Internal state such as queue messages or S3 keys is not asserted directly.

**Coverage target:** All six scenarios above run on every merge to `main` as the release gate.

---

## Test Data Management

- **Unit:** In-code fixtures only; `test-audio.mp3` in `resource-service/src/test/resources/`.
- **Integration (Spock):** Containers start fresh per specification class; no shared mutable state.
- **Component (Cucumber):** WireMock stubs reset between scenarios via `@Before` hook; containers shared across the feature.
- **E2E:** `compose-e2e.yaml` containers start fresh per test run; no pre-seeded data — each scenario provisions its own state via API calls.

---

## Tools Summary

| Category | Tool |
|---|---|
| Unit tests | JUnit 5, Mockito, AssertJ |
| Integration tests | Spock Framework (Groovy), Testcontainers, WireMock |
| Component tests | Cucumber (Gherkin), Spring Boot Test, Testcontainers, WireMock |
| Contract tests | Pact JVM (`pact-jvm-consumer`, `pact-jvm-provider`), Pact Broker |
| E2E tests | Cucumber (Gherkin), Testcontainers `DockerComposeContainer`, RestAssured, Awaitility |
| Coverage report | JaCoCo |
