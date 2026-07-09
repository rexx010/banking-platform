package com.bankplatform.shared.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Money")
class MoneyTest {

    @Nested
    @DisplayName("Creation")
    class Creation{

        @Test
        @DisplayName("ofKobo stores the exact amount")
        void ofKobo_storesExactAmount(){
            Money money = Money.ofkobo(500050L);
            assertThat(money.getAmountInMinorUnits()).isEqualTo(500050L);
            assertThat(money.getCurrency().getCurrencyCode()).isEqualTo("NGN");
        }

        @Test
        @DisplayName("ofNaira converts to kobo correctly")
        void ofNaira_convertsToKoboCorrectly(){
            Money money = Money.ofNaira(new BigDecimal("5000.50"));
            assertThat(money.getAmountInMinorUnits()).isEqualTo(500050L);
        }

        @Test
        @DisplayName("zero returns 0 kobo NGN")
        void zero_returnsZeroNgn(){
            Money zero = Money.zero();
            assertThat(zero.getAmountInMinorUnits()).isZero();
            assertThat(zero.isZero()).isTrue();
        }

        @Test
        @DisplayName("should throw when amount is negative")
        void shouldThrow_whenAmountIsNegative(){
            assertThatThrownBy(() -> Money.ofkobo(-1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be negative");
        }
    }

    @Nested
    @DisplayName("Arithmetic")
    class Arithmetic {

        @Test
        @DisplayName("add returns sum of both amounts")
        void add_returnsSumOfBothAmounts(){
            Money first = Money.ofkobo(100_000L);
            Money second = Money.ofkobo(50_000L);

            Money result = first.add(second);
            assertThat(result.getAmountInMinorUnits()).isEqualTo(150_000L);
        }

        @Test
        @DisplayName("subtract returns correct difference")
        void subtract_returnsCorrectDifference(){
            Money balance = Money.ofkobo(100_000L);
            Money debit = Money.ofkobo(30_000L);

            Money result = balance.subtract(debit);
            assertThat(result.getAmountInMinorUnits()).isEqualTo(70_000L);
        }

        @Test
        @DisplayName("subtract throws when result would be negative")
        void subtract_throwsWhenResultIsNegative(){
            Money balance = Money.ofkobo(10_000L); // ₦100
            Money debit   = Money.ofkobo(20_000L); // ₦200 — more than balance

            assertThatThrownBy(() -> balance.subtract(debit))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("negative");
        }

        @Test
        @DisplayName("add throws when currencies differ")
        void add_throwsWhenCurrenciesDiffer() {
            Money naira  = Money.ofkobo(100_000L);
            Money dollar = Money.of(100_00L, "USD");

            assertThatThrownBy(() -> naira.add(dollar))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Currency mismatch");
        }
    }

    @Nested
    @DisplayName("Comparison")
    class Comparison {

        @Test
        @DisplayName("isGreaterThan returns true when amount is larger")
        void isGreaterThan_returnsTrueWhenLarger() {
            Money large = Money.ofkobo(200_000L);
            Money small = Money.ofkobo(100_000L);

            assertThat(large.isGreaterThan(small)).isTrue();
            assertThat(small.isGreaterThan(large)).isFalse();
        }

        @Test
        @DisplayName("isLessThan works for insufficient funds check")
        void isLessThan_worksForInsufficientFundsCheck() {
            Money balance       = Money.ofkobo(50_000L);
            Money transferAmount = Money.ofkobo(100_000L);

            assertThat(balance.isLessThan(transferAmount)).isTrue();
        }
    }

    @Nested
    @DisplayName("Value object equality")
    class Equality {

        @Test
        @DisplayName("two Money instances with same amount and currency are equal")
        void twoInstances_withSameAmountAndCurrency_areEqual() {
            // Value objects are equal by VALUE not by reference.
            // new Money(500, NGN) == new Money(500, NGN) — always.
            Money a = Money.ofkobo(500L);
            Money b = Money.ofkobo(500L);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("two Money instances with different amounts are not equal")
        void twoInstances_withDifferentAmounts_areNotEqual() {
            Money a = Money.ofkobo(500L);
            Money b = Money.ofkobo(600L);

            assertThat(a).isNotEqualTo(b);
        }
    }

    @Nested
    @DisplayName("Display")
    class Display {

        @Test
        @DisplayName("toMajorUnits converts kobo to naira correctly")
        void toMajorUnits_convertsToBigDecimalCorrectly() {
            Money money = Money.ofkobo(500050L); // ₦5,000.50

            assertThat(money.toMajorUnits()).isEqualByComparingTo("5000.50");
        }

        @Test
        @DisplayName("toString includes currency code and amount")
        void toString_includesCurrencyAndAmount() {
            Money money = Money.ofkobo(100_000L); // ₦1,000

            String str = money.toString();

            assertThat(str).contains("NGN");
            assertThat(str).contains("1000.00");
        }
    }
}