# RunRT — Real‑time Polling & Voting Platform

![Java](https://img.shields.io/badge/Java-17-orange) 
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.4-brightgreen) 
![Docker](https://img.shields.io/badge/Docker-blue) 
![Maven](https://img.shields.io/badge/Maven-3.9.x-C71A36) 
![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-3.7-black) 
![Redis](https://img.shields.io/badge/Redis-7-red) 
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue) 
![WebSockets](https://img.shields.io/badge/WebSockets-STOMP-blueviolet) 
![Zipkin](https://img.shields.io/badge/Zipkin-enabled-lightgrey)

A real‑time, event‑driven system to create polls, ingest votes, and broadcast live results. Built with Spring Boot microservices, Apache Kafka, Redis, WebSockets (STOMP), and a JWT‑secured API gateway — all containerized with Docker Compose for a one‑command local setup.

---

## Key Features
- Event‑driven architecture: HTTP → Kafka for vote ingestion; decoupled producers/consumers
- Live results: Redis‑backed tallies broadcast to clients via STOMP WebSockets
- Secure edge: Spring Cloud Gateway with JWT validation and routing to downstream services
- Docker‑only workflow: all services and infra run as containers via Docker Compose
- Tracing/observability: Zipkin integration and structured logs for request/event correlation

---

## Tech Stack
- Backend: Java 17, Spring Boot 3, Spring Web, Spring Data JPA
- Messaging: Apache Kafka
- Cache/Tally: Redis
- Database: PostgreSQL
- Edge: Spring Cloud Gateway (JWT)
- DevOps: Docker, Docker Compose, Maven

---

## Services & Default Ports
- API Gateway: http://localhost:8080
- Auth Service: http://localhost:8081
- Polls Service: http://localhost:8082
- Voting Service: http://localhost:8083
- Results Service: http://localhost:8084 (WebSocket endpoint: `ws://localhost:8084/ws`)
- Infra (host→container):
  - Kafka: `localhost:9092` → `kafka:9092`
  - PostgreSQL: `localhost:5433` → `postgres:5432`
  - Redis: `localhost:6379` → `redis:6379`
  - Zipkin UI: http://localhost:6334

Note: Services communicate with infra by container DNS (e.g., `postgres:5432`, `kafka:9092`).

---

## Getting Started (Docker‑only)
Prerequisite: Docker Desktop (Windows/macOS) or Docker Engine (Linux).

```cmd
:: From the repo root
docker compose up -d --build

:: Follow logs
docker compose logs -f

:: Tear down when done
docker compose down
```

If health endpoints are enabled, you can check:
- http://localhost:8081/actuator/health
- http://localhost:8082/actuator/health
- http://localhost:8083/actuator/health
- http://localhost:8084/actuator/health

Rebuild just one service (example: results‑service):
```cmd
docker compose stop results-service
docker compose build --no-cache results-service
docker compose up -d results-service
```

---

## Minimal Configuration
Most settings are wired via `docker-compose.yml`. Common environment values:
- Database:
  - `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/postgres`
  - `SPRING_DATASOURCE_USERNAME=postgres`
  - `SPRING_DATASOURCE_PASSWORD=postgres`
- Kafka: `KAFKA_BOOTSTRAP_SERVERS=kafka:9092`
- Zipkin: `SPRING_ZIPKIN_ENDPOINT=http://zipkin:9411/api/v2/spans`
- JWT secrets (Base64‑encoded, ≥32 bytes):
  - `AUTH_JWT_BASE64SECRET` (auth‑service)
  - `GATEWAY_JWT_BASE64SECRET` (api‑gateway)
- Time zone: UTC by default; DB sessions are initialized in UTC

---

## API Overview (via Gateway)
Include `Authorization: Bearer <JWT>` where noted.

Auth
- Register: `POST /api/auth/register`
  - Body: `{ "username": "alice", "password": "password123" }`
- Login: `POST /api/auth/login`
  - Body: `{ "username": "alice", "password": "password123" }`
  - Response: `{ "token": "<JWT>" }`

Polls (secured)
- Create: `POST /api/polls`
  - Body: `{ "title": "Your favorite language?", "options": ["Java","Python","Go"] }`
- List: `GET /api/polls`
- Get by id: `GET /api/polls/{pollId}`
- Update title: `PUT /api/polls/{pollId}` with `{ "title": "Updated" }`

Votes
- Submit: `POST /votes`
  - Body: `{ "pollId": "<uuid>", "optionId": "<uuid>" }`
  - Response: `202 Accepted` e.g., `{ "status": "queued" }`

Results (WebSocket)
- Connect: `ws://localhost:8084/ws`
- Subscribe: `/topic/poll-results/{pollId}`
  - Use a STOMP client (frames must be properly formatted/terminated). Postman is not ideal for STOMP testing.

---

## Quick End‑to‑End Check
1) Register & login to get a JWT (through the gateway)
```cmd
curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"alice\",\"password\":\"password123\"}"

curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"alice\",\"password\":\"password123\"}"
```
2) Create a poll
```cmd
curl -X POST http://localhost:8080/api/polls ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer <JWT>" ^
  -d "{\"title\":\"Your favorite language?\",\"options\":[\"Java\",\"Python\",\"Go\"]}"
```
3) Connect a STOMP client to `ws://localhost:8084/ws` and subscribe to `/topic/poll-results/{pollId}`

4) Cast a vote
```cmd
curl -X POST http://localhost:8080/votes ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer <JWT>" ^
  -d "{\"pollId\":\"<POLL_ID>\",\"optionId\":\"<OPTION_ID>\"}"
```
You should see a real‑time update on the WebSocket subscription.

---

## Troubleshooting (quick)
- Kafka topic/admin timeouts: ensure services use `kafka:9092` (container DNS), not `localhost:9092`
- PostgreSQL connectivity: services use `postgres:5432`; host tools use `localhost:5433`
- JWT key error: use Base64 secrets ≥ 256 bits (32 bytes) and keep auth/gateway in sync
- STOMP/WebSocket: prefer a proper STOMP client; verify subscriptions to `/topic/poll-results/{pollId}`

---

## License
For educational and demonstration purposes.
