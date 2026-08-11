package com.bankplatform.card.application.usecase;

import com.bankplatform.card.domain.model.Card;
import com.bankplatform.card.domain.model.CardNetwork;
import com.bankplatform.card.domain.port.out.CardRepository;
import com.bankplatform.shared.exceptions.BankException;
import com.bankplatform.shared.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.YearMonth;

/**
 * Generates valid card numbers using the Luhn algorithm
 * and computes CVV using HMAC-SHA1.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardGenerator {

    private static final int         MAX_RETRIES = 10;
    private static final SecureRandom RANDOM     = new SecureRandom();

    private final CardRepository cardRepository;

    @Value("${card.cvv-secret}")
    private String cvvSecret;

    /**
     * Generates a unique, Luhn-valid 16-digit card number.
     *
     * Structure:
     *   digits 1-6:  BIN (bank identification number) from CardNetwork
     *   digits 7-15: random unique sequence
     *   digit 16:    Luhn check digit
     */
    public String generateCardNumber(CardNetwork network) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            String candidate = buildCardNumber(network);

            if (!cardRepository.existsByCardNumber(candidate)) {
                log.debug("Generated card number on attempt {}", attempt);
                return candidate;
            }
            log.warn("Card number collision attempt {}", attempt);
        }
        throw new BankException(ErrorCode.INTERNAL_ERROR,
                "Failed to generate unique card number");
    }

    private String buildCardNumber(CardNetwork network) {
        // Start with the 6-digit BIN prefix
        StringBuilder sb = new StringBuilder(network.getBinPrefix());

        // Add 9 random digits (positions 7-15)
        for (int i = 0; i < 9; i++) {
            sb.append(RANDOM.nextInt(10));
        }

        // Compute and append Luhn check digit
        int checkDigit = LuhnAlgorithm.computeCheckDigit(sb.toString());
        sb.append(checkDigit);

        return sb.toString();
    }

    /**
     * Computes the CVV for a card.
     * CVV is NEVER stored — always computed from these inputs.
     *
     * @param cardNumber   16-digit card number
     * @param expiryDate   card expiry month/year
     * @return 3-digit CVV string (zero-padded if needed)
     */
    public String computeCvv(String cardNumber, YearMonth expiryDate) {
        try {
            String message = cardNumber
                    + String.format("%02d", expiryDate.getMonthValue())
                    + String.format("%02d", expiryDate.getYear() % 100);

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(
                    cvvSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));

            byte[] hash = mac.doFinal(
                    message.getBytes(StandardCharsets.UTF_8));

            // Take first 3 digits of the hex representation
            String hex = bytesToHex(hash);
            int cvvInt = Integer.parseInt(hex.substring(0, 6), 16) % 1000;

            return String.format("%03d", cvvInt);

        } catch (Exception ex) {
            throw new RuntimeException("CVV computation failed", ex);
        }
    }

    /**
     * Verifies a CVV by recomputing and comparing.
     * The stored CVV is never used — we compute from scratch.
     */
    public boolean verifyCvv(
            String cardNumber, YearMonth expiryDate, String providedCvv
    ) {
        String expected = computeCvv(cardNumber, expiryDate);
        return expected.equals(providedCvv);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}