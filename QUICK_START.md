# RunRT - Quick Reference Guide

## 🚀 Quick Start (3 Commands)

```powershell
# 1. Start infrastructure
docker-compose up -d

# 2. Build services
mvn clean package -DskipTests

# 3. Use the quick-start script
start.bat
```

## 📡 Service Ports

| Service | Port | Purpose |
|---------|------|---------|
| **API Gateway** | 8080 | Main entry point for all API calls |
| Auth Service | 8081 | User registration & login |
| Polls Service | 8082 | Create & manage polls |
| Voting Service | 8083 | Submit votes |
| Results Service | 8084 | Real-time results & WebSocket |
| PostgreSQL | 5432 | Persistent data storage |
| Redis | 6379 | Real-time vote tallying |
| Kafka | 9092 | Event streaming |
| Zipkin | 9411 | Distributed tracing UI |

## 🔑 API Testing Examples

### 1. Register User
```powershell
curl -X POST http://localhost:8080/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"alice\",\"password\":\"pass123\"}"
```

### 2. Login (Get JWT Token)
```powershell
curl -X POST http://localhost:8080/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"alice\",\"password\":\"pass123\"}"
```
**Response:** `{"token":"eyJhbGciOiJIUzI1NiJ9..."}`

### 3. Create Poll (Use your token)
```powershell
curl -X POST http://localhost:8080/polls ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer YOUR_TOKEN_HERE" ^
  -d "{\"title\":\"Best Framework?\",\"options\":[\"Spring Boot\",\"Django\",\"Express\"]}"
```
**Response:** Contains `pollId` and `optionId` values - save these!

### 4. List All Polls
```powershell
curl http://localhost:8080/polls ^
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 5. Get Specific Poll
```powershell
curl http://localhost:8080/polls/POLL_ID_HERE ^
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 6. Submit Vote
```powershell
curl -X POST http://localhost:8080/votes ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer YOUR_TOKEN_HERE" ^
  -d "{\"pollId\":\"POLL_ID\",\"optionId\":\"OPTION_ID\"}"
```

## 🔍 Monitoring & Debugging

### View Distributed Traces
```
http://localhost:9411
```

### Check Service Health
```powershell
curl http://localhost:8080/actuator/health  # API Gateway
curl http://localhost:8081/actuator/health  # Auth Service
curl http://localhost:8082/actuator/health  # Polls Service
curl http://localhost:8083/actuator/health  # Voting Service
curl http://localhost:8084/actuator/health  # Results Service
```

### View Kafka Topics
```powershell
docker exec -it runrt-kafka-1 kafka-topics --list --bootstrap-server localhost:9092
```

### View Kafka Messages
```powershell
# View poll creation events
docker exec -it runrt-kafka-1 kafka-console-consumer ^
  --bootstrap-server localhost:9092 ^
  --topic polls-created-topic ^
  --from-beginning

# View vote events
docker exec -it runrt-kafka-1 kafka-console-consumer ^
  --bootstrap-server localhost:9092 ^
  --topic votes-topic ^
  --from-beginning
```

### Check Redis Data
```powershell
docker exec -it runrt-redis-1 redis-cli

# Inside Redis CLI:
KEYS *                          # See all keys
GET poll:POLL_ID:total          # Total votes for a poll
GET poll:POLL_ID:option:OPT_ID  # Votes for specific option
```

### View Logs
```powershell
docker-compose logs kafka
docker-compose logs postgres
docker-compose logs redis
docker-compose logs zipkin
```

## 🌊 Data Flow

```
1. User registers/logs in → Auth Service → Returns JWT
2. User creates poll → Polls Service → PostgreSQL + Kafka Event
3. User votes → Voting Service → Kafka Event
4. Kafka Event → Voting Service Consumer → PostgreSQL (persistence)
5. Kafka Event → Results Service Consumer → Redis (aggregation)
6. Results Service → WebSocket Broadcast → Connected Clients
```

## 🎯 Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                      API GATEWAY :8080                      │
│              (JWT Validation & Routing)                     │
└─────────────────────────────────────────────────────────────┘
         │              │              │              │
         ▼              ▼              ▼              ▼
    ┌────────┐    ┌─────────┐   ┌─────────┐   ┌──────────┐
    │  Auth  │    │  Polls  │   │ Voting  │   │ Results  │
    │  :8081 │    │  :8082  │   │  :8083  │   │  :8084   │
    └────────┘    └─────────┘   └─────────┘   └──────────┘
         │              │              │              │
         │              ▼              │              │
         │         ┌────────┐          │              │
         │         │ Kafka  │◄─────────┘              │
         │         │ :9092  │─────────────────────────┘
         │         └────────┘
         │              │
         ▼              │              ┌────────┐
    ┌──────────┐        └─────────────►│ Redis  │
    │PostgreSQL│                       │ :6379  │
    │  :5432   │                       └────────┘
    └──────────┘
         
         All services trace to → Zipkin :9411
```

## 📊 Kafka Topics

| Topic | Producer | Consumer | Purpose |
|-------|----------|----------|---------|
| `polls-created-topic` | Polls Service | (optional) | Notify when new poll is created |
| `votes-topic` | Voting Service | Voting + Results Services | Vote events for processing |

## 🗄️ Database Schema

### Auth Service (PostgreSQL)
```sql
users (id, username, password_hash, role)
```

### Polls Service (PostgreSQL)
```sql
polls (id, title)
poll_options (id, poll_id, text)
```

### Voting Service (PostgreSQL)
```sql
votes (poll_id, option_id, user_id, created_at)
```

### Results Service (Redis)
```
poll:{pollId}:total = count
poll:{pollId}:option:{optionId} = count
```

## 🐛 Common Issues

| Problem | Solution |
|---------|----------|
| Port already in use | Change port in `application.yml` or kill the process |
| Kafka connection refused | Wait 30s after `docker-compose up`, Kafka is slow to start |
| JWT signature invalid | Ensure all services use same secret |
| Database connection failed | Check PostgreSQL is running: `docker ps` |
| Redis timeout | Restart Redis: `docker-compose restart redis` |

## 🔐 Security Notes

⚠️ **DEVELOPMENT ONLY** - Current JWT secrets are hardcoded for demo purposes.

For production:
1. Use environment variables for secrets
2. Enable HTTPS/TLS
3. Add rate limiting
4. Implement proper CORS
5. Use secure password hashing (already using BCrypt ✅)

## 📚 Next Steps

See `ACTION_PLAN.md` for:
- Detailed testing procedures
- Enhancement recommendations
- Production readiness checklist
- Troubleshooting guide

## 🎓 Key Technologies

- **Spring Boot 3.3.4** - Application framework
- **Spring Cloud Gateway** - API gateway
- **Apache Kafka** - Event streaming
- **PostgreSQL 16** - Relational database
- **Redis 7** - In-memory cache
- **WebSocket/STOMP** - Real-time communication
- **JWT** - Authentication tokens
- **Zipkin** - Distributed tracing
- **Docker Compose** - Container orchestration

---

**Pro Tip**: Use the `start.bat` script for easy management of the entire system!

