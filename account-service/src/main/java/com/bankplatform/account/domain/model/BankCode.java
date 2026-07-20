package com.bankplatform.account.domain.model;

import java.util.Objects;
import java.util.Set;

public final class BankCode {

    // Nigerian bank codes from the CBN NUBAN specification
    private static final Set<String> KNOWN_CODES = Set.of(
            "011", // First Bank
            "014", // Afribank
            "023", // Citibank
            "032", // Union Bank
            "033", // United Bank for Africa
            "035", // Wema Bank
            "040", // Equitorial Trust Bank
            "044", // Access Bank
            "050", // Ecobank
            "056", // Oceanic Bank
            "057", // Zenith Bank
            "058", // Guaranty Trust Bank
            "063", // Diamond Bank
            "068", // Standard Chartered Bank
            "069", // Intercontinental Bank
            "070", // Fidelity Bank
            "076", // Skye Bank
            "082", // BankPhb
            "084", // SpringBank
            "085", // Finbank
            "214", // FCMB
            "215", // Unity Bank
            "221", // StanbicIBTC
            "232"  // Sterling Bank
    );

    private final String value;

    public BankCode(String value) {
        Objects.requireNonNull(value, "Bank code must not be null");
        String trimmed = value.trim();
        if (!trimmed.matches("\\d{3}")) {
            throw new IllegalArgumentException(
                    "Bank code must be exactly 3 digits, got: " + trimmed
            );
        }
        this.value = trimmed;
    }

    public String getValue() { return value; }

    public boolean isKnownCode() {
        return KNOWN_CODES.contains(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BankCode other)) return false;
        return value.equals(other.value);
    }

    @Override public int hashCode() { return value.hashCode(); }
    @Override public String toString() { return value; }
}