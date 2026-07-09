package com.bankplatform.shared.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public final class Money {
    private final long amountInMinorUnits;
    private final Currency currency;

    private Money(long amountInMinorUnits, Currency currency){
        if(amountInMinorUnits < 0){
            throw new IllegalArgumentException("Money amount cannot be negative: " + amountInMinorUnits);
        }
        this.amountInMinorUnits = amountInMinorUnits;
        this.currency = Objects.requireNonNull(currency, "Currency must not be null");
    }

    /** Creates Money from kobo (the primary way — used when reading from DB) */
    public static Money of(long kobo, String currency){
        return new Money(kobo, Currency.getInstance(currency));
    }

    /**Convenience factory for Nigerian Naira*/
    public static Money ofkobo(long kobo){
        return new Money(kobo, Currency.getInstance("NGN"));
    }

    /**Creates Money from a naira amount — only use for display/input conversion*/
    public static Money ofNaira(BigDecimal naira){
        Objects.requireNonNull(naira, "Naira amount must not be null");
        long kobo = naira
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
        return ofkobo(kobo);
    }

    /**Zero balance — used when opening a new account*/
    public static Money zero(){
        return ofkobo(0L);
    }

    /**Arithmetic*/
    public Money add(Money other){
        assertSameCurrency(other);
        return new Money(this.amountInMinorUnits + other.amountInMinorUnits, this.currency);
    }

    public Money subtract(Money other){
        assertSameCurrency(other);
        long result = this.amountInMinorUnits - other.amountInMinorUnits;
        if(result < 0){
            throw new IllegalArgumentException(
                    "Subtraction would result in negative amount: " + this + " - " + other
            );
        }
        return new Money(result, this.currency);
    }

    /**Comparison*/
    public boolean isGreaterThan(Money other){
        assertSameCurrency(other);
        return this.amountInMinorUnits > other.amountInMinorUnits;
    }

    public boolean isGreaterThanOrEqual(Money other){
        assertSameCurrency(other);
        return this.amountInMinorUnits >= other.amountInMinorUnits;
    }

    public boolean isLessThan(Money other){
        assertSameCurrency(other);
        return this.amountInMinorUnits < other.amountInMinorUnits;
    }

    public boolean isZero(){
        return amountInMinorUnits == 0;
    }

    /** Accessor*/
    /** Use this when saving to the database */
    public long getAmountInMinorUnits(){
        return amountInMinorUnits;
    }

    public Currency getCurrency(){
        return currency;
    }

    /** Convert to naira for display only — never use for arithmetic */
    public BigDecimal toMajorUnits(){
        int fractionDigits = currency.getDefaultFractionDigits();
        return BigDecimal.valueOf(amountInMinorUnits)
                .divide(BigDecimal.TEN.pow(fractionDigits),
                        fractionDigits, RoundingMode.UNNECESSARY);
    }

    /**Jackson support*/
    @JsonCreator
    public static Money fromJson(
            @JsonProperty("amountInMinorUnits") long amount,
            @JsonProperty("currency") String currency
    ){
        return Money.of(amount, currency);
    }
    /**Object contracts*/
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof Money other)) return false;
        return amountInMinorUnits == other.amountInMinorUnits
                && currency.equals(other.currency);
    }

    @Override
    public int hashCode(){
        return Objects.hash(amountInMinorUnits, currency);
    }

    @Override
    public String toString(){
        return "%s %s (%d kobo)".formatted(
                currency.getCurrencyCode(),
                toMajorUnits().toPlainString(),
                amountInMinorUnits
        );
    }

    private void assertSameCurrency(Money other){
        if(!this.currency.equals(other.currency)){
            throw new IllegalArgumentException(
                    "Currency mismatch: cannot mix %s and %s"
                            .formatted(this.currency, other.currency)
            );
        }
    }
}
