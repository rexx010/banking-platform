package com.bankplatform.transfer.domain.port.out;

import java.util.Optional;

/**
 * OUT-PORT: stores idempotency keys to prevent duplicate transfers.
 *
 * Implemented using Redis with 24-hour TTL.
 *
 * Flow:
 *   1. Client sends transfer with idempotencyKey=abc123
 *   2. We check: has abc123 been processed?
 *   3. No → process transfer → store result under abc123 (24hr TTL)
 *   4. Client retries with same idempotencyKey=abc123
 *   5. Yes → return cached result, no transfer executed
 */
public interface IdempotencyStore {

    /**
     * Stores the transfer ID for this idempotency key.
     * TTL is 24 hours — keys auto-expire after that.
     */
    void store(String idempotencyKey, String transferId);

    /**
     * Returns the transfer ID if this key was already processed.
     * Returns empty if this is a new (non-duplicate) request.
     */
    Optional<String> findTransferId(String idempotencyKey);
}