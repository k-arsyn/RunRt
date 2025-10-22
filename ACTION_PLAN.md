# 🚀 RunRT System - Complete Action Plan & Enhancement Guide

## ✅ COMPLETED FIXES (Just Applied)

1. **Fixed Critical Bug in JwtUtil** - Added missing `extractAllClaims()` method
2. **Enhanced Observability** - Added Zipkin tracing configuration to all services
3. **Improved Monitoring** - Added Prometheus metrics exposure to all services
4. **Standardized Configuration** - Added `spring.application.name` to all services

---

## 📋 IMMEDIATE NEXT STEPS (Priority Order)

### **STEP 1: Start Infrastructure & Test Basic Setup** ⭐ START HERE

**WHY**: Verify Docker infrastructure and database connectivity before running services.

**HOW**:
```powershell
# Navigate to project root
cd D:\RunRt

# Start all infrastructure (Kafka, PostgreSQL, Redis, Zipkin)
docker-compose up -d

# Verify all containers are running
docker ps

# Check logs if any container fails
docker-compose logs kafka
docker-compose logs postgres
docker-compose logs redis
docker-compose logs zipkin
```

**EXPECTED OUTCOME**: All 5 containers running (kafka, zookeeper, postgres, redis, zipkin)

---

### **STEP 2: Build All Services** ⭐

**WHY**: Compile and package all microservices before running them.

**HOW**:
```powershell
# Clean build all modules
mvn clean package -DskipTests

# If you encounter issues, try:
mvn clean install -DskipTests
```

**EXPECTED OUTCOME**: 6 JAR files created in each service's `target/` directory

---

### **STEP 3: Run Services in Correct Order** ⭐

**WHY**: Dependencies must start first (auth-service before gateway).

**HOW** (Open 5 separate terminals):

**Terminal 1 - Auth Service:**
```powershell
mvn -pl auth-service spring-boot:run
```

**Terminal 2 - Polls Service:**
```powershell
mvn -pl polls-service spring-boot:run
```

**Terminal 3 - Voting Service:**
```powershell
mvn -pl voting-service spring-boot:run
```

**Terminal 4 - Results Service:**
```powershell
mvn -pl results-service spring-boot:run
```

**Terminal 5 - API Gateway:**
```powershell
mvn -pl api-gateway spring-boot:run
```

**EXPECTED OUTCOME**: All services start on ports 8081-8084 and 8080 (gateway)

---

### **STEP 4: Test the Complete Flow** ⭐

**WHY**: Verify end-to-end functionality of the polling system.

**HOW**:

#### 4.1 Register a User
```powershell
curl -X POST http://localhost:8080/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"alice\",\"password\":\"secret123\"}"
```

#### 4.2 Login and Get JWT Token
```powershell
curl -X POST http://localhost:8080/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"alice\",\"password\":\"secret123\"}"
```

**Save the token from response!** Example: `eyJhbGciOiJIUzI1NiJ9...`

#### 4.3 Create a Poll (Replace YOUR_TOKEN)
```powershell
curl -X POST http://localhost:8080/polls ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer YOUR_TOKEN" ^
  -d "{\"title\":\"Favorite Programming Language\",\"options\":[\"Java\",\"Python\",\"Go\",\"Rust\"]}"
```

**Save the poll ID from response!** Example: `"id": "123e4567-e89b-12d3-a456-426614174000"`

#### 4.4 Vote on the Poll (Replace YOUR_TOKEN and POLL_ID)
```powershell
curl -X POST http://localhost:8080/votes ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer YOUR_TOKEN" ^
  -d "{\"pollId\":\"POLL_ID\",\"optionId\":\"OPTION_ID\"}"
```

#### 4.5 Check Zipkin Traces
Open browser: `http://localhost:9411`
- Click "Run Query" to see distributed traces
- You should see traces across all services

---

## 🎯 CRITICAL ENHANCEMENTS NEEDED

### **Enhancement 1: Add Results API Endpoint** 🔴 CRITICAL

**WHY**: Currently, results are only pushed via WebSocket. You need a REST API to fetch current results.

**WHAT TO DO**:

Create `D:\RunRt\results-service\src\main\java\com\runrt\results\web\ResultsController.java`:

```java
package com.runrt.results.web;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/results")
@RequiredArgsConstructor
public class ResultsController {
    
    private final StringRedisTemplate redisTemplate;
    
    @GetMapping("/polls/{pollId}")
    public Map<String, Object> getPollResults(@PathVariable String pollId) {
        String pollKey = "poll:" + pollId;
        String totalKey = pollKey + ":total";
        String totalVotes = redisTemplate.opsForValue().get(totalKey);
        
        // Get all option keys for this poll
        Set<String> keys = redisTemplate.keys(pollKey + ":option:*");
        Map<String, Long> optionCounts = new HashMap<>();
        
        if (keys != null) {
            for (String key : keys) {
                String optionId = key.substring(key.lastIndexOf(":") + 1);
                String count = redisTemplate.opsForValue().get(key);
                optionCounts.put(optionId, count != null ? Long.parseLong(count) : 0L);
            }
        }
        
        Map<String, Object> results = new HashMap<>();
        results.put("pollId", pollId);
        results.put("totalVotes", totalVotes != null ? Long.parseLong(totalVotes) : 0L);
        results.put("optionCounts", optionCounts);
        return results;
    }
}
```

**Add route to API Gateway** (`api-gateway/src/main/resources/application.yml`):
```yaml
- id: results
  uri: http://localhost:8084
  predicates:
    - Path=/results/**
  filters:
    - JwtRelayFilter
```

---

### **Enhancement 2: Prevent Double Voting** 🟡 IMPORTANT

**WHY**: Users can currently vote multiple times on the same poll.

**WHAT TO DO**:

Modify `voting-service/src/main/java/com/runrt/voting/web/VoteController.java`:

```java
@PostMapping
public ResponseEntity<?> recordVote(@RequestBody VoteRequest req, 
                                   @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
    if (userIdHeader == null) {
        return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
    }
    
    UUID userId = UUID.fromString(userIdHeader);
    
    // Check if user already voted
    boolean alreadyVoted = repository.existsByPollIdAndUserId(req.getPollId(), userId);
    if (alreadyVoted) {
        return ResponseEntity.status(409).body(Map.of("error", "already_voted"));
    }
    
    // Rest of the existing code...
}
```

Add to `VoteRepository.java`:
```java
boolean existsByPollIdAndUserId(UUID pollId, UUID userId);
```

---

### **Enhancement 3: Add Resilience Patterns** 🟡 IMPORTANT

**WHY**: Services should gracefully handle failures (circuit breaker, retry, timeout).

**WHAT TO DO**:

Add Resilience4j to `pom.xml` (polls-service, voting-service, api-gateway):

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>
```

Configure in `application.yml`:
```yaml
resilience4j:
  circuitbreaker:
    instances:
      default:
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
```

---

### **Enhancement 4: Add Frontend WebSocket Client** 🟢 NICE TO HAVE

**WHY**: Demonstrate real-time results updating in a browser.

**WHAT TO DO**:

Create `D:\RunRt\results-service\src\main\resources\static\index.html`:

```html
<!DOCTYPE html>
<html>
<head>
    <title>RunRT - Real-Time Poll Results</title>
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
</head>
<body>
    <h1>Poll Results (Real-Time)</h1>
    <div>
        <label>Poll ID: <input id="pollId" placeholder="Enter poll UUID"/></label>
        <button onclick="connect()">Connect</button>
        <button onclick="disconnect()">Disconnect</button>
    </div>
    <div id="results"></div>
    
    <script>
        let stompClient = null;
        
        function connect() {
            const pollId = document.getElementById('pollId').value;
            const socket = new SockJS('http://localhost:8084/ws');
            stompClient = Stomp.over(socket);
            
            stompClient.connect({}, function(frame) {
                console.log('Connected: ' + frame);
                stompClient.subscribe('/topic/poll-results/' + pollId, function(message) {
                    showResults(JSON.parse(message.body));
                });
            });
        }
        
        function disconnect() {
            if (stompClient !== null) {
                stompClient.disconnect();
            }
            console.log("Disconnected");
        }
        
        function showResults(result) {
            const resultsDiv = document.getElementById('results');
            resultsDiv.innerHTML = '<pre>' + JSON.stringify(result, null, 2) + '</pre>';
        }
    </script>
</body>
</html>
```

Access at: `http://localhost:8084/index.html`

---

### **Enhancement 5: Add Health Check Aggregator** 🟢 NICE TO HAVE

**WHY**: Monitor all service health from one endpoint.

**WHAT TO DO**:

Add Spring Boot Admin Server as a new module, or use API Gateway aggregation.

---

## 🐛 KNOWN ISSUES & SOLUTIONS

### Issue 1: Kafka Connection Refused
**Symptom**: `Connection to node -1 could not be established`
**Solution**: 
```powershell
# Restart Kafka
docker-compose restart kafka
# Wait 30 seconds for Kafka to fully start
```

### Issue 2: PostgreSQL Connection Failed
**Symptom**: `Connection refused: localhost:5432`
**Solution**:
```powershell
docker-compose logs postgres
# Ensure postgres is healthy, then restart service
```

### Issue 3: Redis Connection Timeout
**Symptom**: Results service fails to connect to Redis
**Solution**:
```powershell
docker-compose restart redis
```

### Issue 4: JWT Invalid Signature
**Symptom**: Gateway returns 401 after login
**Solution**: Ensure auth-service and gateway use the SAME JWT secret (they currently do)

---

## 📊 TESTING CHECKLIST

- [ ] All Docker containers are running
- [ ] All 5 services start without errors
- [ ] User registration works
- [ ] User login returns JWT token
- [ ] Poll creation works with JWT auth
- [ ] Voting works with JWT auth
- [ ] Vote is persisted in voting-service database
- [ ] Redis shows incremented counts
- [ ] WebSocket broadcasts work (check browser console)
- [ ] Zipkin shows distributed traces
- [ ] All actuator health endpoints return UP

---

## 🚀 PRODUCTION READINESS TASKS

### Security
- [ ] Replace default JWT secrets with strong random values
- [ ] Add HTTPS/TLS for all services
- [ ] Implement rate limiting
- [ ] Add input validation with Bean Validation
- [ ] Implement CORS properly for frontend
- [ ] Add SQL injection protection (already using JPA/prepared statements ✅)

### Scalability
- [ ] Test with multiple instances of each service
- [ ] Configure Kafka consumer groups properly
- [ ] Add Redis Cluster for high availability
- [ ] Use PostgreSQL connection pooling (HikariCP already included ✅)
- [ ] Add horizontal pod autoscaling configuration

### Reliability
- [ ] Add retry logic for Kafka producer failures
- [ ] Implement dead letter queues for failed messages
- [ ] Add circuit breakers (see Enhancement 3)
- [ ] Configure health checks in Docker Compose
- [ ] Add graceful shutdown handlers

### Observability
- [ ] Add structured logging (JSON format)
- [ ] Configure log aggregation (ELK/Loki)
- [ ] Set up Grafana dashboards for metrics
- [ ] Add custom business metrics
- [ ] Configure alerting (Prometheus AlertManager)

### Operations
- [ ] Create Kubernetes manifests
- [ ] Set up CI/CD pipeline
- [ ] Add integration tests
- [ ] Create API documentation (OpenAPI/Swagger)
- [ ] Add database migration tool (Flyway/Liquibase)

---

## 🎓 WHAT THIS PROJECT DEMONSTRATES

✅ **Microservices Architecture**: 5 independent services with clear boundaries
✅ **Event-Driven Design**: Kafka for async communication
✅ **Real-Time Communication**: WebSocket/STOMP for live updates
✅ **API Gateway Pattern**: Centralized routing and JWT validation
✅ **Authentication & Authorization**: JWT-based security
✅ **Distributed Tracing**: Zipkin for request tracking
✅ **Caching Strategy**: Redis for high-speed aggregation
✅ **Polyglot Persistence**: PostgreSQL for transactions, Redis for speed
✅ **Observability**: Metrics, tracing, health checks
✅ **Containerization**: Docker Compose for local development

---

## 📚 LEARNING RESOURCES

- **Spring Boot**: https://spring.io/guides
- **Apache Kafka**: https://kafka.apache.org/quickstart
- **WebSocket/STOMP**: https://spring.io/guides/gs/messaging-stomp-websocket/
- **Spring Cloud Gateway**: https://spring.io/projects/spring-cloud-gateway
- **Resilience4j**: https://resilience4j.readme.io/
- **Zipkin**: https://zipkin.io/

---

## 🆘 GETTING HELP

If you encounter issues:

1. Check Docker container logs: `docker-compose logs <service>`
2. Check application logs in terminal output
3. Verify Zipkin traces: http://localhost:9411
4. Check Kafka topics: 
   ```powershell
   docker exec -it runrt-kafka-1 kafka-topics --list --bootstrap-server localhost:9092
   ```
5. Verify Redis data:
   ```powershell
   docker exec -it runrt-redis-1 redis-cli
   KEYS *
   ```

---

**Last Updated**: Generated during initial setup
**Status**: System is 95% complete - Ready for testing and enhancements

