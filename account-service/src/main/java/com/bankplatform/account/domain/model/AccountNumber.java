package com.bankplatform.account.domain.model;

import java.util.Objects;

public final class AccountNumber {
    private final String value;

    public AccountNumber(String value) {
        Objects.requireNonNull(value, "Account number must not be null");
        String trimmed = value.trim();
        if ( !trimmed.matches("\\d{10}")) {
            throw new IllegalArgumentException(
                    "Account number must be exactly 10 digits, got: " + trimmed
            );
        }
        this.value = trimmed;
    }

    public String getValue() { return value; }

    /** for logging — shows only last 4 digits */
    public String masked() {
        return "******" + value.substring(6);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountNumber other)) return false;
        return value.equals(other.value);
    }

    @Override public int hashCode() { return value.hashCode(); }

    /** toString is masked — safe to use directly in logs */
    @Override public String toString() { return masked(); }
}
