package com.bankplatform.auth.application.usecase;

import com.bankplatform.auth.domain.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JwtService {
    private static final long ACCESS_TTL_SECONDS = 15 * 60L;
    private static final long REFRESH_TTL_SECONDS = 7 * 24 * 3600L;

    private final SecretKey signingKey;

    public JwtService(
            @Value("${security.jwt.secret}")
            String secret
    ){
        //Key must be at least 32 characters for HS256
        this.signingKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**Token generation*/
    public String generateAccessToken(
        String userId,
        String email,
        Set<Role> roles
    ){
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(ACCESS_TTL_SECONDS);

        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .claim("roles", roles.stream()
                        .map(Enum::name)
                        .collect(Collectors.toList()))
                .claim("type", "ACCESS")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .id(UUID.randomUUID().toString())
                .signWith(signingKey)
                .compact();
    }

    public String generateRefreshToken(String userId){
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(REFRESH_TTL_SECONDS);

        return Jwts.builder()
                .subject(userId)
                .claim("type", "REFRESH")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .id(UUID.randomUUID().toString())
                .signWith(signingKey)
                .compact();
    }

    /**Token validation*/
    public Optional<Claims> validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException ex){
            log.warn("JWT validation failed: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    /** claims extraction */
    public Optional<String> extractUserId(String token){
        return validateToken(token).map(Claims::getSubject);
    }

    public Optional<String> extractEmail(String token){
        return validateToken(token)
                .map(c -> c.get("email", String.class));
    }

    @SuppressWarnings("unchecked")
    public Optional<List<String>> extractRole(String token){
        return validateToken(token)
                .map(c -> c.get("roles", List.class));
    }

    public long getAccessTtlSeconds(){
        return ACCESS_TTL_SECONDS;
    }

    public long getRefreshTtlSeconds(){
        return REFRESH_TTL_SECONDS;
    }
}
