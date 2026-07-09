package com.bankplatform.auth.adapter.out.persistence;

import com.bankplatform.auth.domain.port.out.RefreshTokenStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Implements RefreshTokenStore using Redis.
 *
 * Key structure:
 *   refresh_token:{token_value} → {userId}
 *
 * TTL is set on every write — Redis automatically deletes
 * the key after the TTL expires. No scheduled cleanup needed.
 *
 * Logout works by deleting the key — the JWT signature is
 * still valid but the store lookup fails, so no new access
 * tokens can be issued using that refresh token.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private static final String PREFIX = "refresh_token";
    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(String userId, String refreshToken, long ttlSeconds) {
        redisTemplate.opsForValue().set(
                PREFIX + refreshToken,
                userId,
                Duration.ofSeconds(ttlSeconds)
        );
        log.debug("Saved refresh token userId={} ttl={}s",
                userId, ttlSeconds);
    }

    @Override
    public Optional<String> findUserIdByToken(String refreshToken) {
        String userId = redisTemplate.opsForValue()
                .get(PREFIX + refreshToken);
        return Optional.ofNullable(userId);
    }

    @Override
    public void delete(String refreshToken) {
        redisTemplate.delete(PREFIX + refreshToken);
        log.debug("Deleted refresh token");
    }

    @Override
    public void deleteAllForUser(String userId) {
        /*
         * Scan all refresh_token:* keys and delete those
         * belonging to this user. Scan is O(N) but this
         * operation only happens on "logout from all devices"
         * which is rare — acceptable cost.
         */
        var keys = redisTemplate.keys(PREFIX + "*");
        if (keys == null || keys.isEmpty()) return;
        long deleted = keys.stream()
                .filter(key -> userId.equals(
                        redisTemplate.opsForValue().get(keys)))
                .peek(redisTemplate::delete)
                .count();
        log.info("Deleted {} refresh token for userId={}",
                deleted, userId);
    }
}
