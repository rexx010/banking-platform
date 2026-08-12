package com.bankplatform.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;;

/**
 * Gateway filter that validates JWT tokens before routing requests.
 *
 * Applied to routes that require authentication (all except
 * register, login, and refresh).
 *
 * If the token is missing or invalid:
 *   → Returns 401 Unauthorized immediately
 *   → Request never reaches the downstream service
 *
 * If the token is valid:
 *   → Extracts userId from token
 *   → Adds X-User-Id header to the forwarded request
 *   → Downstream service can read userId from this header
 */
@Slf4j
@Component
public class JwtAuthenticationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {


    private final SecretKey signingKey;

    public JwtAuthenticationGatewayFilterFactory(
            @Value("${security.jwt.secret}") String secret
    ) {
        super(Config.class);
        this.signingKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst("Authorization");

            // No token — reject immediately
            if (authHeader == null
                    || !authHeader.startsWith("Bearer ")) {
                return unauthorised(exchange,
                        "Missing Authorization header");
            }

            String token = authHeader.substring(7);

            try {
                Claims claims = Jwts.parser()
                        .verifyWith(signingKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                String userId = claims.getSubject();

                // Forward userId to downstream service as a header
                // The service can read it without re-validating the JWT
                var mutatedRequest = exchange.getRequest()
                        .mutate()
                        .header("X-User-Id", userId)
                        .build();

                log.debug("JWT valid userId={} path={}",
                        userId,
                        exchange.getRequest().getPath());

                return chain.filter(
                        exchange.mutate()
                                .request(mutatedRequest)
                                .build()
                );

            } catch (JwtException | IllegalArgumentException ex) {
                log.warn("JWT validation failed: {}", ex.getMessage());
                return unauthorised(exchange, "Invalid or expired token");
            }
        };
    }

    private Mono<Void> unauthorised(
            ServerWebExchange exchange, String reason
    ) {
        log.warn("Rejected request — {}: {}",
                reason, exchange.getRequest().getPath());
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // No config needed — secret comes from @Value
    }
}