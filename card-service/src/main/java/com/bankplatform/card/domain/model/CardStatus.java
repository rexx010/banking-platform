package com.bankplatform.card.domain.model;

/**
 * Card lifecycle states.
 *
 * ACTIVE:   card can be used for transactions
 * FROZEN:   temporarily blocked by the customer (e.g. misplaced card)
 *           can be unfrozen by the customer at any time
 * BLOCKED:  permanently blocked — card cannot be unfrozen
 *           a new card must be issued (e.g. reported stolen)
 * EXPIRED:  past expiry date — transactions automatically declined
 */
public enum CardStatus {
    ACTIVE,
    FROZEN,
    BLOCKED,
    EXPIRED
}