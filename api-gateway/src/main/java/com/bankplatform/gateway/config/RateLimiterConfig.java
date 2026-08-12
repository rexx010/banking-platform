package com.bankplatform.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * Rate limiter key resolvers.
 *
 * ipKeyResolver:   limit by client IP — for public endpoints
 * userKeyResolver: limit by userId from JWT — for authenticated endpoints
 */
@Configuration
public class RateLimiterConfig {

    /**
     * Resolves the rate limit bucket by the client's IP address.
     * Used on public endpoints where we do not have a userId yet.
     *
     * If X-Forwarded-For is present (behind load balancer),
     * use the original client IP.
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            var forwardedFor = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-Forwarded-For");

            String ip = forwardedFor != null
                    ? forwardedFor.split(",")[0].trim()
                    : (exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress()
                    .getAddress().getHostAddress()
                    : "unknown");

            return Mono.just("ip:" + ip);
        };
    }

    /**
     * Resolves the rate limit bucket by the userId in the JWT.
     * Used on authenticated endpoints so each user has their own limit.
     * Falls back to IP if userId header is not present.
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-User-Id");

            if (userId != null && !userId.isBlank()) {
                return Mono.just("user:" + userId);
            }

            // Fallback to IP for unauthenticated requests
            String ip = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress()
                    .getAddress().getHostAddress()
                    : "unknown";

            return Mono.just("ip:" + ip);
        };
    }
}