package com.bankplatform.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway — the single entry point for all client requests.
 *
 * Responsibilities:
 *   1. Route requests to the correct downstream service
 *   2. Validate JWT tokens at the edge (before routing)
 *   3. Rate limit requests to prevent abuse
 *   4. Apply CORS headers for browser clients
 *   5. Provide circuit breaking if a service is down
 *
 * Uses Netty (reactive, non-blocking) not Tomcat.
 * Can handle thousands of concurrent connections with
 * far fewer threads than a blocking server.
 */
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}