# CLAUDE.md — microservices-ecommerce

## Output Rule
Always list every changed file at end of response. Format:
```
Changed files:
- path/to/File.java
- path/to/Other.java
```

---

## Project Overview

Spring Boot 4.0.3 microservices ecommerce platform. Java 21. Spring Cloud 2025.1.1.

**Architecture:** Event-driven with Kafka, Outbox pattern, dual-DB strategy (PostgreSQL + MongoDB), Redis cache, Eureka discovery, Gateway routing.

---

## Service Map

| Service | Port | DB | Role |
|---------|------|----|------|
| `api-gateway` | 8080 | — | Spring Cloud Gateway + Eureka client |
| `service-discovery` | 8761 | — | Eureka server |
| `order-service` | 8083 | PostgreSQL 5433 | Order lifecycle, publishes ORDER_CREATED |
| `inventory-service` | 8084 | PostgreSQL 5434 | Reserves stock, two-pass validation |
| `payment-service` | 8085 | PostgreSQL 5435 | Payment processing (in-progress) |
| `product-service` | 8082 | MongoDB 27017 + Redis | Product catalog, cache-aside |
| `user-service` | 8081 | MongoDB 27018 | User management |
| `common-dtos` | — | — | Shared event/topic contracts |

---

## Event Flow

```
POST /api/v1/orders
  → order-service creates Order + OutboxEvent(ORDER_CREATED)
  → OutboxPublisher @Scheduled(3000ms) → Kafka topic: ORDER_CREATED

  → inventory-service OrderCreationConsumer
      Pass 1: validate all items have stock
        fail → OutboxEvent(INVENTORY_UNAVAILABLE) → Kafka
        pass → Pass 2: update inventory + save reservations
               OutboxEvent(INVENTORY_RESERVED) → Kafka

  → order-service InventoryUnavailableConsumer
      → Order status = CANCELLED
```

### Kafka Topics (common-dtos/Topics.java)
- `ORDER_CREATED`
- `INVENTORY_RESERVED`
- `INVENTORY_RESERVED`
- `INVENTORY_RELEASED`
- `INVENTORY_UNAVAILABLE`
- `PRODUCT_CREATED`

### Kafka Setup
- 3-broker KRaft cluster (no ZooKeeper), ports 9092/9093/9094
- Idempotent producer enabled
- Consumer isolation: `READ_COMMITTED`
- Partitions: 3, replication factor: 3, min.insync.replicas: 2
- Dead-letter recovery: retryable vs non-retryable split

---

## Core Patterns

### Outbox Pattern (order-service + inventory-service)
Every service that publishes Kafka events:
1. Write event to `OutboxEvent` table **in same transaction** as domain write
2. `OutboxPublisher` @Scheduled(fixedDelay=3000ms) polls `OutboxStatus.NEW`
3. Publish → mark `SENT`. Retry with backoff → `DEAD` after 3 failures

`OutboxEvent` fields: `aggregateType`, `aggregateId`, `eventType`, `payload` (JSONB), `status`, `retryCount`

Never publish Kafka events directly from service layer. Always go through Outbox.

### Two-Pass Inventory Reservation
`InventoryServiceImpl.reserveOrder()`:
- Pass 1: validate ALL items — if any fails, abort entire reservation
- Pass 2: atomic update inventory quantities + save `InventoryReservation` records
- Whole method `@Transactional`

### CQRS Split
Services split into read/write interfaces:
- `IOrderReadService` / `IOrderWriteService`
- `IInventoryReadService` / `IInventoryWriteService`
- `IProductReadService` / `IProductWriteService`

Implementations may merge but interfaces stay separate.

### Cache-Aside (product-service)
Redis via Spring Cache. Cache names in `CacheNames.java` constants. Always annotate at service layer, not controller.

---

## Package Structure Convention

Each service follows:
```
com.niladri.<service_name>/
  config/          # KafkaConfig, AsyncConfig, TracingConfig, etc.
  controller/      # REST controllers
  service/         # Interfaces: IXxxService, IXxxReadService, IXxxWriteService
  service/impl/    # Implementations
  repository/      # JPA/Mongo repositories
  model/           # Entities + Enums
  dto/             # Request/Response DTOs + ApiResponse wrapper
  consumers/       # Kafka @KafkaListener classes
  producers/       # IGenericEventProducer + GenericEventProducer
  publisher/       # OutboxPublisher
  exception/       # Custom exceptions + GlobalExceptionHandler
  error/           # Retryable / NonRetryable markers
  constant/        # Constants (e.g. InventoryStatus)
```

---

## Database Conventions

### PostgreSQL Services (order, inventory, payment)
- Entities extend `BaseModel` (id, createdAt, updatedAt with `@EntityListeners(AuditingEntityListener.class)`)
- Enable `@EnableJpaAuditing` on main application class
- `OutboxEvent.payload` is JSONB — use `@Type(JsonBinaryType.class)` (Hibernate Types)
- Schema: `spring.jpa.hibernate.ddl-auto=update` (dev). Use Flyway for prod.

### MongoDB Services (product, user)
- Documents extend `Auditable` base class
- Use `@Document` not `@Entity`
- Separate `MongoConfig.java` for custom converters if needed

---

## Error Handling Convention

Every service has:
- `GlobalExceptionHandler` — `@RestControllerAdvice`, returns `ApiResponse` wrapper
- Custom exceptions extend `RuntimeException`
- Kafka error handling: `DefaultErrorHandler` with `FixedBackOff` + `DeadLetterPublishingRecoverer`
- `Retryable` / `NonRetryable` marker annotations on exceptions

---

## DTO / API Response Convention

All REST responses wrapped in `ApiResponse<T>`:
```java
ApiResponse.success(data)
ApiResponse.error(message)
```

Request DTOs use `@Valid` + Bean Validation annotations. Never expose entities directly.

---

## New Service Checklist

When adding a new service:
- [ ] Add to `pom.xml` modules list (parent aggregator)
- [ ] Add Eureka client dependency + `@EnableDiscoveryClient`
- [ ] Add `application.yaml` with: app name, DB config, Kafka bootstrap servers, Zipkin tracing
- [ ] Add `KafkaConfig.java` with producer/consumer factories
- [ ] Add `GlobalExceptionHandler.java`
- [ ] Add `BaseModel.java` (PostgreSQL) or `Auditable.java` (MongoDB)
- [ ] Add `OutboxEvent.java` + `OutboxPublisher.java` if publishing events
- [ ] Register routes in `api-gateway` application.yaml
- [ ] Add service to `docker-compose.yml`

---

## New Kafka Event Checklist

When adding a new event type:
- [ ] Add topic constant to `common-dtos/Topics.java`
- [ ] Add enum value to `common-dtos/EventType.java`
- [ ] Create event class in `common-dtos/events/` with `EventMetadata` field
- [ ] Rebuild `common-dtos` before dependent services
- [ ] Create `NewTopicBean` in producer service's `KafkaConfig.java`
- [ ] Add `@KafkaListener` consumer in receiving service
- [ ] Route through Outbox — never publish inline

---

## Observability

- **Distributed tracing:** Micrometer + Zipkin at `http://localhost:9411`
- **Logging:** Logstash pipeline → Elasticsearch (ELK stack)
- Config: each service has `management.tracing.sampling.probability=1.0` + Zipkin endpoint

---

## In-Progress Work

| Service | Status | What's missing |
|---------|--------|----------------|
| `payment-service` | Models only | Service layer, repositories, controllers, Kafka consumers |
| `api-gateway` | Skeleton | Route config in application.yaml |
| Circuit breaker | Not added | Resilience4j needed for inter-service HTTP calls |

---

## Known Issues

- Duplicate `ApiGatewayApplication.java` under both `com.example.*` and `com.niladri.*` packages — clean up `com.example` package
- Payment DB not active in docker-compose (commented out) — add when implementing payment service
- No Flyway migrations — currently using `ddl-auto=update`, add migrations before any prod deployment

---

## Commands

```bash
# Start infrastructure
docker-compose up -d

# Build all
mvn clean install

# Build common-dtos first (other services depend on it)
cd common-dtos && mvn clean install

# Run single service
cd order-service && mvn spring-boot:run
```

---

## Tech Stack Reference

| Concern | Technology |
|---------|-----------|
| Framework | Spring Boot 4.0.3, Spring Cloud 2025.1.1 |
| Language | Java 21 |
| Messaging | Apache Kafka (KRaft, 3-broker) |
| Relational DB | PostgreSQL (per-service) |
| Document DB | MongoDB |
| Cache | Redis (Lettuce pool) |
| Service Discovery | Netflix Eureka |
| API Gateway | Spring Cloud Gateway Server MVC |
| Tracing | Micrometer + Zipkin |
| Logging | Logstash + Elasticsearch |
| Build | Maven (multi-module) |
| Containerization | Docker Compose |
