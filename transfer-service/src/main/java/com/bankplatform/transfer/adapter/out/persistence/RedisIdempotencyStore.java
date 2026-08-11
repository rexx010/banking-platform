package com.bankplatform.transfer.adapter.out.persistence;

import com.bankplatform.transfer.domain.port.out.IdempotencyStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis-backed idempotency store.
 *
 * Key structure: transfer_idempotency:{idempotencyKey} → {transferId}
 * TTL: 24 hours — after which the key expires and a new transfer
 * with the same key would be treated as a fresh request.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisIdempotencyStore implements IdempotencyStore {

    private static final String  PREFIX = "transfer_idempotency:";
    private static final Duration TTL   = Duration.ofHours(24);

    private final StringRedisTemplate redis;

    @Override
    public void store(String idempotencyKey, String transferId) {
        redis.opsForValue().set(
                PREFIX + idempotencyKey, transferId, TTL);
        log.debug("Stored idempotency key={} transferId={}",
                idempotencyKey, transferId);
    }

    @Override
    public Optional<String> findTransferId(String idempotencyKey) {
        String transferId = redis.opsForValue()
                .get(PREFIX + idempotencyKey);
        return Optional.ofNullable(transferId);
    }
}