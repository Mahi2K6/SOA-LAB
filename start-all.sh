#!/usr/bin/env bash
# Smart Tourism Portal - start Eureka, 3 tourist-service instances and the API gateway (Linux/macOS)
# Run from the project root after:  mvn clean package -DskipTests
set -e
mkdir -p logs

java -jar eureka-server/target/eureka-server-1.0.0.jar > logs/eureka.log 2>&1 &
echo "Eureka starting (pid $!)"; sleep 20

for PORT in 8081 8082 8083; do
  java -jar tourist-service/target/tourist-service-1.0.0.jar --server.port=$PORT > logs/tourist-$PORT.log 2>&1 &
  echo "tourist-service:$PORT starting (pid $!)"
done
sleep 15

java -jar api-gateway/target/api-gateway-1.0.0.jar > logs/gateway.log 2>&1 &
echo "API gateway starting (pid $!)"

echo
echo "Eureka dashboard : http://localhost:8761"
echo "Gateway          : http://localhost:8080/api/tourist/instance"
echo "Wait ~30s for registration, then run ./test-load-balancing.sh"
echo "Stop everything  : pkill -f -- '-1.0.0.jar'"
