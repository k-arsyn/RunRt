# RunRT: Real-Time Distributed Polling/Voting System

This is a multi-module Spring Boot project showcasing microservices + Kafka + WebSockets.

Modules:
- `common`: shared DTOs and JWT utility
- `auth-service`: JWT auth with register/login (PostgreSQL)
- `polls-service`: CRUD for polls + Kafka producer
- `voting-service`: ingest votes -> Kafka and persist
- `results-service`: consume votes -> Redis tally + WebSocket broadcast
- `api-gateway`: Spring Cloud Gateway with JWT filter

## Prereqs
- Java 17+
- Maven 3.9+
- Docker Desktop

## Start infrastructure

```powershell
# From repo root
docker compose up -d
```

## Build all services

```powershell
mvn -q -DskipTests package
```

## Run a service (example)

```powershell
mvn -pl auth-service spring-boot:run
```

Wire services to infra via `application.yml` (to be filled): Kafka `localhost:9092`, Postgres DBs per service, Redis `localhost:6379`.

## Notes
- Default JWT secret in `auth-service` is for dev only.
- Add environment-specific configs and Dockerfiles to containerize services.
