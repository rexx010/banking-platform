package com.bankplatform.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

/**
 * Global filter that logs every request through the gateway.
 *
 * Runs for ALL routes — no configuration needed per route.
 * Logs: method, path, status code, duration in milliseconds.
 *
 * ORDER = -1 means this runs FIRST (lower = earlier).
 * Running first lets us measure the full round-trip duration.
 */
@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain
    ) {
        Instant start  = Instant.now();
        var     request = exchange.getRequest();

        String ip = request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        log.info("→ {} {} from {}",
                request.getMethod(), request.getPath(), ip);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            long durationMs = Duration.between(start, Instant.now())
                    .toMillis();

            log.info("← {} {} {} {}ms",
                    request.getMethod(), request.getPath(),
                    response.getStatusCode(), durationMs);
        }));
    }
}