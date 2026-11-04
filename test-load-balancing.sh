#!/usr/bin/env bash
# Sends 9 requests through the API gateway and counts which instance answered each one.

echo "Instances registered in Eureka (as seen by the gateway):"
curl -s http://localhost:8080/gateway/services/tourist-service; echo; echo

for i in $(seq 1 9); do
  served=$(curl -s http://localhost:8080/api/tourist/instance | sed -E 's/.*"servedBy":"([^"]+)".*/\1/')
  echo "Request $i -> $served"
  echo "$served" >> /tmp/lb-results.$$
done

echo; echo "Distribution:"
sort /tmp/lb-results.$$ | uniq -c
rm -f /tmp/lb-results.$$
