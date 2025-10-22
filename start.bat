@echo off
cd /d %~dp0

REM ===========================================
REM RunRT - Real-Time Polling System Starter
REM ===========================================

echo ========================================
echo  RunRT - Real-Time Polling System
echo  Quick Start Script (UTC timezone)
echo ========================================
echo.

REM Resolve docker compose command (v2 or legacy)
set "DC=docker compose"
%DC% version >nul 2>&1 || set "DC=docker-compose"

:menu
echo.
echo Choose an option:
echo.
echo 1. Start Infrastructure (Docker)
echo 2. Build All Services
echo 3. Start All Services (5 terminals)
echo 4. Stop Infrastructure (Manual - see instructions)
echo 5. View Service Status
echo 6. Clean and Rebuild
echo 7. Exit
echo.
set /p choice="Enter your choice (1-7): "

if "%choice%"=="1" goto start_infra
if "%choice%"=="2" goto build
if "%choice%"=="3" goto start_services
if "%choice%"=="4" goto stop_infra
if "%choice%"=="5" goto status
if "%choice%"=="6" goto clean_build
if "%choice%"=="7" goto end
goto menu

:start_infra
echo.
echo Checking Docker installation...
docker --version >nul 2>&1 || (
  echo.
  echo ERROR: Docker Desktop is not installed or not on PATH.
  echo Please install Docker Desktop for Windows and enable WSL2 backend.
  pause
  goto menu
)

echo Starting Docker infrastructure...
%DC% up -d --build
echo.
echo Waiting for services to be ready (30 seconds)...
timeout /t 30 /nobreak >nul
echo.
echo Checking container status:
docker ps
echo.
echo Infrastructure started!
echo - Kafka:        localhost:3447
echo - PostgreSQL:   localhost:5433 (UTC timezone)
echo - Redis:        localhost:6379
echo - Zipkin UI:    http://localhost:6334
pause
goto menu

:build
echo.
echo Checking Maven installation...
where mvn >nul 2>&1 || (
  echo.
  echo ERROR: Maven (mvn) not found on PATH.
  echo Install Apache Maven or build inside Docker with: %DC% build
  pause
  goto menu
)

echo Building all services (UTC timezone set via env)...
set MAVEN_OPTS=-Duser.timezone=UTC
mvn clean package -DskipTests
echo.
echo Build complete! JAR files created in each service's target/ folder.
pause
goto menu

:start_services
echo.
echo Starting all services via Docker Compose...
echo This will build and start all microservices in Docker containers.
echo.
%DC% up -d --build
echo.
echo Waiting for services to be ready (45 seconds)...
timeout /t 45 /nobreak >nul
echo.
echo All services are running in Docker!
echo.
echo Service URLs:
echo - API Gateway:     http://localhost:8080
echo - Auth Service:    http://localhost:8081
echo - Polls Service:   http://localhost:8082
echo - Voting Service:  http://localhost:8083
echo - Results Service: http://localhost:8084
echo - Zipkin UI:       http://localhost:6334
echo.
echo Useful commands:
echo   View all logs:        %DC% logs -f
echo   View service logs:    %DC% logs -f [service-name]
echo   Example:              %DC% logs -f auth-service
echo.
echo Container status:
docker ps --format "table {{.Names}}\t{{.Status}}"
pause
goto menu

:stop_infra
echo.
echo ============================================
echo MANUAL STOP - Containers NOT automatically stopped
echo ============================================
echo.
echo To stop containers manually, use:
echo.
echo   Stop all:     %DC% stop
echo   Stop one:     docker stop ^<container-name^>
echo.
echo To remove containers and volumes completely:
echo   %DC% down -v
echo.
echo Current running containers:
docker ps --format "table {{.Names}}\t{{.Status}}"
echo.
pause
goto menu

:status
echo.
echo Docker Container Status:
docker ps -a --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo.
echo.
echo Testing Service Health (if running):
echo.
echo Auth Service:
curl -s http://localhost:8081/actuator/health 2>nul || echo [Not responding]
echo.
echo.
echo Polls Service:
curl -s http://localhost:8082/actuator/health 2>nul || echo [Not responding]
echo.
echo.
echo Voting Service:
curl -s http://localhost:8083/actuator/health 2>nul || echo [Not responding]
echo.
echo.
echo Results Service:
curl -s http://localhost:8084/actuator/health 2>nul || echo [Not responding]
echo.
echo.
echo API Gateway:
curl -s http://localhost:8080/actuator/health 2>nul || echo [Not responding]
echo.
pause
goto menu

:check_logs
echo.
echo ========================================
echo Checking Service Logs for Errors
echo ========================================
echo.
echo AUTH SERVICE:
docker logs --tail 20 runrt-auth-service-1 2>&1 | findstr /C:"Started" /C:"Error" /C:"Exception" /C:"Failed"
echo.
echo POLLS SERVICE:
docker logs --tail 20 runrt-polls-service-1 2>&1 | findstr /C:"Started" /C:"Error" /C:"Exception" /C:"Failed"
echo.
echo VOTING SERVICE:
docker logs --tail 20 runrt-voting-service-1 2>&1 | findstr /C:"Started" /C:"Error" /C:"Exception" /C:"Failed"
echo.
echo RESULTS SERVICE:
docker logs --tail 20 runrt-results-service-1 2>&1 | findstr /C:"Started" /C:"Error" /C:"Exception" /C:"Failed"
echo.
echo API GATEWAY:
docker logs --tail 20 runrt-api-gateway-1 2>&1 | findstr /C:"Started" /C:"Error" /C:"Exception" /C:"Failed"
echo.
echo.
echo To view full logs for a service:
echo   %DC% logs -f [service-name]
echo   Example: %DC% logs -f auth-service
pause
goto menu

:clean_build
echo.
echo Cleaning and rebuilding all services (UTC timezone)...
set MAVEN_OPTS=-Duser.timezone=UTC
mvn clean install -DskipTests
echo.
echo Clean build complete!
pause
goto menu

:end
echo.
echo Goodbye!
echo.
echo Remember: Docker containers are still running.
echo Use "%DC% stop" to stop them.
exit
