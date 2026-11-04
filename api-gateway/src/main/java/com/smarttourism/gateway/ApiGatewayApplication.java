package com.smarttourism.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Single entry point for the Smart Tourism Portal (port 8080).
 * Requests to /api/tourist/** are sent to lb://tourist-service:
 * the gateway looks the name up in Eureka and spreads requests
 * round-robin across every healthy instance.
 */
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
