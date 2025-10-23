# RunRT Platform

A real-time, distributed polling and voting platform built with Spring Boot microservices, Apache Kafka, Redis, WebSockets (STOMP), and Spring Cloud Gateway. The system demonstrates modern event-driven architecture, JWT-based security, and live result streaming.

---

## Contents
- Overview
- Architecture at a glance
- Services and ports
- Data & messaging
- Quick start (Docker-only)
- Rebuild just one service
- Configuration (env vars)
- API quick reference
- Real-time results (WebSocket)
- End-to-end test walkthrough
- Troubleshooting playbook

---

## Overview
RunRT splits responsibilities across dedicated services:
- Auth: user registration and JWT login
- Polls: create/list/read/update polls, produces events
- Voting: high-throughput vote ingestion, produces vote events
- Results: consumes vote events, tallies in Redis, broadcasts live via WebSocket
- API Gateway: single entry point, routing + JWT relay

The platform is designed to run fully in Docker for consistency and easy onboarding.

---

## Architecture at a glance
```mermaid
flowchart LR
  subgraph Client
    UI[Web / Postman]
  end

  subgraph Edge[API Gateway]
    GW[Spring Cloud Gateway]
  end

  subgraph Core[Microservices]
    AUTH[auth-service\nJWT]
    POLLS[polls-service\nCRUD + Kafka]
    VOTE[voting-service\nKafka Producer]
    RES[results-service\nKafka Consumer + Redis + WS]
  end

  subgraph Infra[Infrastructure]
    PG[(PostgreSQL)]
    KAFKA[(Kafka)]
    REDIS[(Redis)]
    ZIPKIN[Zipkin]
  end

  UI -->|HTTP| GW
  GW -->|/api/auth/**| AUTH
  GW -->|/api/polls/**| POLLS
  GW -->|/votes| VOTE

  AUTH <--> PG
  POLLS <--> PG
  VOTE -->|VoteRecordedEvent| KAFKA
  RES -->|consume votes-topic| KAFKA
  RES <--> REDIS
  RES -->|/topic/poll-results/{pollId}| UI

  GW --> ZIPKIN
  AUTH --> ZIPKIN
  POLLS --> ZIPKIN
  VOTE --> ZIPKIN
  RES --> ZIPKIN
```

---

## Services and ports
- API Gateway: http://localhost:8080
- Auth Service: http://localhost:8081
- Polls Service: http://localhost:8082
- Voting Service: http://localhost:8083
- Results Service: http://localhost:8084 (WebSocket endpoint: `ws://localhost:8084/ws`)
- Kafka broker (host): `localhost:9092` (inside Docker network: `kafka:9092`)
- PostgreSQL (host): `localhost:5433` → container `postgres:5432`
- Redis (host): `localhost:6379` → container `redis:6379`
- Zipkin UI: http://localhost:6334

Note: Microservices talk to infra using container DNS names (e.g., `postgres:5432`, `kafka:9092`, `redis:6379`).

---

## Data & messaging
- PostgreSQL: persistent storage for auth and polls
- Apache Kafka: event backbone
  - Topic: `votes-topic` (VoteRecordedEvent)
  - Producer: voting-service
  - Consumer: results-service
- Redis: fast in-memory tallies for results-service
- WebSocket (STOMP): results-service broadcasts on `/topic/poll-results/{pollId}`

---

## Quick start (Docker-only)
Prerequisites: Docker Desktop (Windows/macOS) or Docker Engine (Linux).

```cmd
:: From repo root
docker compose up -d --build

:: Check containers
docker ps

:: Follow all logs
docker compose logs -f
```

Health checks (if enabled):
- http://localhost:8081/actuator/health
- http://localhost:8082/actuator/health
- http://localhost:8083/actuator/health
- http://localhost:8084/actuator/health

Stop the stack:
```cmd
docker compose down
```

---

## Rebuild just one service
When you edit code for a single service, rebuild and restart only that service:

```cmd
:: Example: results-service
docker compose stop results-service
docker compose build --no-cache results-service
docker compose up -d results-service

:: Tail recent logs
docker logs runrt-results-service-1 --tail 100
```

This keeps everything in containers, as intended.

---

## Configuration (env vars)
Core environment wiring is defined in `docker-compose.yml`. Services expect Docker-network addresses by default.

Common variables you may see:
- Database
  - `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/postgres`
  - `SPRING_DATASOURCE_USERNAME=postgres`
  - `SPRING_DATASOURCE_PASSWORD=postgres`
- Kafka
  - `KAFKA_BOOTSTRAP_SERVERS=kafka:9092`
- Zipkin (tracing)
  - `SPRING_ZIPKIN_ENDPOINT=http://zipkin:9411/api/v2/spans`
- JWT (examples; ensure 256-bit+ secrets)
  - Auth service: `AUTH_JWT_BASE64SECRET`
  - Gateway: `GATEWAY_JWT_BASE64SECRET`

Time zone: services default to UTC. Database sessions are initialized with UTC to avoid drift.

---

## API quick reference
All examples are through the Gateway (port 8080). Include `Authorization: Bearer <token>` where noted.

Auth Service
- Register: `POST /api/auth/register`
  - Body: `{ "username": "alice", "password": "password123" }`
  - 200: `{ "status": "registered" }`
- Login: `POST /api/auth/login`
  - Body: `{ "username": "alice", "password": "password123" }`
  - 200: `{ "token": "<JWT>" }`

Polls Service (secured)
- Create poll: `POST /api/polls`
  - Body: `{ "title": "Your favorite language?", "options": ["Java","Python","Go"] }`
- List polls: `GET /api/polls`
- Get by id: `GET /api/polls/{pollId}`
- Update title: `PUT /api/polls/{pollId}` with `{ "title": "Updated" }`

Voting Service
- Submit a vote: `POST /votes`
  - Body: `{ "pollId": "<uuid>", "optionId": "<uuid>" }`
  - 202: `{ "status": "queued" }`

Results Service (WebSocket)
- Endpoint: `ws://localhost:8084/ws`
- Topic to subscribe: `/topic/poll-results/{pollId}`

---

## Real-time results (WebSocket)
For a reliable test client, a minimal STOMP page is bundled:
- Open: http://localhost:8084/ws-test.html
- Click "Connect"
- Enter your `pollId` and press "Subscribe"
- Submit votes (see examples above) and watch messages appear live

Advanced (Postman): STOMP requires frames terminated with a null byte. Postman’s raw text messages typically omit this; prefer the test page or a proper STOMP client.

---

## End-to-end test walkthrough
1) Register & login
```cmd
curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"alice\",\"password\":\"password123\"}"

curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"alice\",\"password\":\"password123\"}"
```
Copy the `token` from the login response.

2) Create a poll
```cmd
curl -X POST http://localhost:8080/api/polls ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer <TOKEN>" ^
  -d "{\"title\":\"Your favorite language?\",\"options\":[\"Java\",\"Python\",\"Go\"]}"
```
Save the returned `pollId` and one `optionId`.

3) Open the test page and subscribe
- http://localhost:8084/ws-test.html → Connect → Subscribe to `/topic/poll-results/{pollId}`

4) Cast a vote
```cmd
curl -X POST http://localhost:8080/votes ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer <TOKEN>" ^
  -d "{\"pollId\":\"<POLL_ID>\",\"optionId\":\"<OPTION_ID>\"}"
```
You should see the update arrive on the page immediately.

---

## Troubleshooting playbook
- Kafka “timed out waiting to get existing topics”
  - Ensure services use `spring.kafka.bootstrap-servers=kafka:9092` (inside Docker). Rebuild only the affected service.
- 404 on `/api/votes`
  - Gateway route must match controller path. This repo routes `/votes/**` to `voting-service`.
- PostgreSQL connection refused / wrong port
  - Services must use `postgres:5432`. Host tools (pgAdmin/DBeaver) use `localhost:5433`.
- Postgres timezone error “Asia/Calcutta”
  - Use `Asia/Kolkata` or keep default UTC. This repo standardizes on UTC.
- JWT key length error (api-gateway)
  - Provide a Base64-encoded secret of at least 32 bytes (256 bits). Keep auth and gateway in sync.
- WebSocket not receiving messages
  - Use the bundled test page (`ws-test.html`). If still quiet, tail logs:
    - `docker logs runrt-voting-service-1 --tail 200`
    - `docker logs runrt-results-service-1 --tail 200`
  - Look for “Kafka vote received …” and “WS sent to …”. If missing, recheck Kafka bootstrap addresses.

---

## Contributing
- Fork the repo and create feature branches per service
- Keep changes scoped; rebuild only the affected service
- Add small integration tests where practical (Kafka/topic boundaries, controller mappings)

---

## License
This project is provided for educational and demonstration purposes.
