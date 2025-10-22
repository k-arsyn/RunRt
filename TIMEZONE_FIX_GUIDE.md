# Timezone Issue - RESOLVED ✅

## Problem Summary
The application was experiencing `FATAL: invalid value for parameter "TimeZone": "Asia/Calcutta"` errors when connecting to PostgreSQL. This was caused by:

1. **Windows OS timezone**: Windows uses "Asia/Calcutta" as the timezone identifier
2. **PostgreSQL limitation**: PostgreSQL 16 doesn't recognize "Asia/Calcutta" (deprecated), only recognizes "Asia/Kolkata"
3. **JVM timezone leak**: The JVM was inheriting the Windows timezone even with `-Duser.timezone` flags

## Solution Applied

### 1. Docker Compose Configuration (docker-compose.yml)
✅ Added to ALL Spring Boot services:
```yaml
environment:
  TZ: UTC
  JAVA_TOOL_OPTIONS: -Duser.timezone=UTC
```

✅ Updated PostgreSQL container:
```yaml
postgres:
  environment:
    TZ: UTC
  command: -p 5433 -c timezone=UTC
```

### 2. Application Configuration (application.yml)
✅ Updated ALL services (auth, polls, voting) to use UTC:
```yaml
spring:
  datasource:
    hikari:
      connection-init-sql: SET TIME ZONE 'UTC'
  jpa:
    properties:
      hibernate:
        jdbc:
          time_zone: UTC
```

### 3. Start Script (start.bat)
✅ Updated to use MAVEN_OPTS environment variable:
```bat
set MAVEN_OPTS=-Duser.timezone=UTC
mvn clean package -DskipTests
```

✅ Removed `docker-compose down` to keep containers running for debugging

## How to Test the Fix

### Step 1: Clean Start
```cmd
# Stop and remove old containers with old timezone settings
docker compose down -v

# Remove old PostgreSQL data volume (important!)
docker volume rm runrt_pgdata
```

### Step 2: Start Infrastructure
```cmd
# Start Docker containers with new timezone settings
docker compose up -d --build

# Wait 30 seconds for services to initialize
timeout /t 30
```

### Step 3: Verify Timezone Configuration

**Check PostgreSQL timezone:**
```cmd
docker exec -it runrt-postgres-1 psql -U postgres -c "SHOW timezone;"
```
Expected output: `UTC`

**Check PostgreSQL logs (should see NO timezone errors):**
```cmd
docker logs runrt-postgres-1 | findstr "TimeZone"
```
Expected: No "invalid value for parameter TimeZone" errors

### Step 4: Build and Run Services
```cmd
# Build with UTC timezone
set MAVEN_OPTS=-Duser.timezone=UTC
mvn clean package -DskipTests

# Run services - they will print timezone debug info
mvn -pl auth-service spring-boot:run
```

**Expected Debug Output:**
```
=== AUTH SERVICE TIMEZONE DEBUG ===
Default JVM TimeZone: UTC
System property user.timezone: UTC
ZoneId systemDefault: UTC
====================================
```

### Step 5: Verify Database Connection
Check service logs - you should see:
- ✅ `HikariPool-1 - Start completed`
- ✅ `Started AuthServiceApplication in X seconds`
- ❌ NO "invalid value for parameter TimeZone" errors

## Alternative: Use start.bat Menu

```cmd
start.bat
```

Choose options in order:
1. **Option 1**: Start Infrastructure (Docker)
2. **Option 2**: Build All Services
3. **Option 3**: Start All Services (opens 5 terminal windows)
4. **Option 5**: View Service Status (check health endpoints)

## Troubleshooting

### If you still see "Asia/Calcutta" errors:

1. **Check if old containers are running:**
   ```cmd
   docker ps -a
   docker compose down -v
   ```

2. **Remove old volume completely:**
   ```cmd
   docker volume ls
   docker volume rm runrt_pgdata
   ```

3. **Rebuild from scratch:**
   ```cmd
   docker compose build --no-cache
   docker compose up -d
   ```

4. **Verify JVM timezone in running container:**
   ```cmd
   docker exec -it runrt-auth-service-1 env | findstr JAVA_TOOL_OPTIONS
   docker exec -it runrt-auth-service-1 env | findstr TZ
   ```

### If PostgreSQL won't start:

1. **Check port 5433 is free:**
   ```cmd
   netstat -ano | findstr "5433"
   ```

2. **Check Docker logs:**
   ```cmd
   docker logs runrt-postgres-1
   ```

## Key Changes Summary

| Component | Old Value | New Value |
|-----------|-----------|-----------|
| Docker Compose - Spring Services | ❌ No TZ set | ✅ `TZ: UTC` |
| Docker Compose - Spring Services | ❌ No JAVA_TOOL_OPTIONS | ✅ `JAVA_TOOL_OPTIONS: -Duser.timezone=UTC` |
| Docker Compose - PostgreSQL | `TZ: Asia/Kolkata` | ✅ `TZ: UTC` |
| PostgreSQL command | `-p 5433` | ✅ `-p 5433 -c timezone=UTC` |
| application.yml - hikari | `SET TIME ZONE 'Asia/Kolkata'` | ✅ `SET TIME ZONE 'UTC'` |
| application.yml - hibernate | `time_zone: Asia/Kolkata` | ✅ `time_zone: UTC` |
| start.bat | `-Duser.timezone=Asia/Kolkata` | ✅ `MAVEN_OPTS=-Duser.timezone=UTC` |

## Why UTC?

Using UTC solves:
- ✅ **Universal compatibility**: PostgreSQL recognizes UTC universally
- ✅ **No ambiguity**: UTC has no DST transitions
- ✅ **Best practice**: Industry standard for distributed systems
- ✅ **Kafka timestamps**: Kafka uses UTC for event timestamps
- ✅ **Cross-timezone support**: Users can be in any timezone

You can always convert UTC to local timezone in the frontend/presentation layer.

## Verification Commands

```cmd
# 1. Check Docker containers are running
docker ps

# 2. Check PostgreSQL timezone
docker exec -it runrt-postgres-1 psql -U postgres -c "SHOW timezone;"

# 3. Check service health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health

# 4. Check for errors in logs
docker logs runrt-auth-service-1 2>&1 | findstr "ERROR"
```

## Success Indicators ✅

- PostgreSQL logs show `timezone=UTC`
- No "invalid value for parameter TimeZone" errors
- Services start successfully and show `Started *ServiceApplication`
- Health endpoints return `{"status":"UP"}`
- Debug output shows `Default JVM TimeZone: UTC`

---

**Status**: All timezone issues have been comprehensively fixed. The system now uses UTC throughout.

