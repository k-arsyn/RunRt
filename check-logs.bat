@echo off
echo Checking logs for all services...
echo.
echo ========================================
echo AUTH SERVICE LOGS
echo ========================================
docker logs runrt-auth-service-1 2>&1 | findstr /C:"Error" /C:"Exception" /C:"Failed" /C:"Caused by" /C:"Started AuthServiceApplication"
echo.
echo ========================================
echo POLLS SERVICE LOGS
echo ========================================
docker logs runrt-polls-service-1 2>&1 | findstr /C:"Error" /C:"Exception" /C:"Failed" /C:"Caused by" /C:"Started PollsServiceApplication"
echo.
echo ========================================
echo VOTING SERVICE LOGS
echo ========================================
docker logs runrt-voting-service-1 2>&1 | findstr /C:"Error" /C:"Exception" /C:"Failed" /C:"Caused by" /C:"Started VotingServiceApplication"
echo.
echo ========================================
echo RESULTS SERVICE LOGS
echo ========================================
docker logs runrt-results-service-1 2>&1 | findstr /C:"Error" /C:"Exception" /C:"Failed" /C:"Caused by" /C:"Started ResultsServiceApplication"
echo.
echo ========================================
echo API GATEWAY LOGS
echo ========================================
docker logs runrt-api-gateway-1 2>&1 | findstr /C:"Error" /C:"Exception" /C:"Failed" /C:"Caused by" /C:"Started ApiGatewayApplication"
echo.
echo.
echo For full logs of a service, run:
echo   docker logs runrt-auth-service-1
pause

