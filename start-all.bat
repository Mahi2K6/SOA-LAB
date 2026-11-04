@echo off
REM Smart Tourism Portal - start Eureka, 3 tourist-service instances and the API gateway (Windows)
REM Run from the project root after:  mvn clean package -DskipTests

start "Eureka Server :8761" cmd /k java -jar eureka-server\target\eureka-server-1.0.0.jar
echo Waiting for Eureka to start...
timeout /t 20 /nobreak >nul

start "Tourist Service :8081" cmd /k java -jar tourist-service\target\tourist-service-1.0.0.jar --server.port=8081
start "Tourist Service :8082" cmd /k java -jar tourist-service\target\tourist-service-1.0.0.jar --server.port=8082
start "Tourist Service :8083" cmd /k java -jar tourist-service\target\tourist-service-1.0.0.jar --server.port=8083
timeout /t 15 /nobreak >nul

start "API Gateway :8080" cmd /k java -jar api-gateway\target\api-gateway-1.0.0.jar

echo.
echo Eureka dashboard : http://localhost:8761
echo Gateway          : http://localhost:8080/api/tourist/instance
echo Give it ~30 seconds for all instances to register, then run test-load-balancing.ps1
