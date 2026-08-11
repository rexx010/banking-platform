package com.bankplatform.card.application.usecase;

/**
 * Luhn algorithm implementation for card number validation and generation.
 *
 * Algorithm (validation):
 *   1. Starting from the rightmost digit, double every second digit
 *   2. If doubling produces > 9, subtract 9
 *   3. Sum all digits
 *   4. If sum % 10 == 0, the number is valid
 *
 * Algorithm (check digit computation):
 *   1. Apply steps 1-3 to the first 15 digits
 *   2. Check digit = (10 - (sum % 10)) % 10
 */
public final class LuhnAlgorithm {

    private LuhnAlgorithm() {}

    public static boolean validate(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16
                || !cardNumber.matches("\\d{16}")) {
            return false;
        }

        int sum = 0;
        boolean doubleIt = false;

        // Process from right to left
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));

            if (doubleIt) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }

            sum += digit;
            doubleIt = !doubleIt;
        }

        return sum % 10 == 0;
    }

    /**
     * Computes the Luhn check digit for a 15-digit partial card number.
     *
     * @param first15 the first 15 digits of the card number
     * @return the check digit (0-9) to append
     */
    public static int computeCheckDigit(String first15) {
        if (first15 == null || first15.length() != 15
                || !first15.matches("\\d{15}")) {
            throw new IllegalArgumentException(
                    "Input must be exactly 15 digits");
        }

        int sum = 0;
        boolean doubleIt = true; // rightmost of 15 is doubled (position 2 from right of final 16)

        for (int i = first15.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(first15.charAt(i));

            if (doubleIt) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }

            sum += digit;
            doubleIt = !doubleIt;
        }

        return (10 - (sum % 10)) % 10;
    }
}