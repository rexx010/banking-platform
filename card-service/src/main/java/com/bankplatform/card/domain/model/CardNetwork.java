package com.bankplatform.card.domain.model;

/**
 * Card payment network.
 *
 * The BIN (Bank Identification Number — first 6 digits) identifies
 * both the network and the issuing bank.
 *
 * For our platform:
 *   VERVE:      Nigeria domestic network — lower transaction fees
 *   VISA:       International network
 *   MASTERCARD: International network
 *
 * In production, BIN ranges are assigned by the card networks
 * and cannot be chosen freely. For development we use illustrative values.
 */
public enum CardNetwork {
    VERVE      ("650002"),  // Verve BIN prefix (illustrative)
    VISA       ("411111"),  // Visa BIN prefix (illustrative)
    MASTERCARD ("512345"); // Mastercard BIN prefix (illustrative)

    private final String binPrefix;

    CardNetwork(String binPrefix) {
        this.binPrefix = binPrefix;
    }

    public String getBinPrefix() { return binPrefix; }
}