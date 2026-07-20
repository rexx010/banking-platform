package com.bankplatform.account.application.usecase;

import com.bankplatform.account.domain.model.AccountNumber;
import com.bankplatform.account.domain.model.BankCode;
import com.bankplatform.account.domain.port.out.AccountRepository;
import com.bankplatform.shared.exceptions.BankException;
import com.bankplatform.shared.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class NubanGenerator {

    private static final int[] WEIGHTS = {3,7,3,3,7,3,3,7,3,3,7,3};
    private static final int MAX_RETRIES = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AccountRepository accountRepository;

    /**
     * Generates a unique NUBAN for the given bank code.
     *
     * @param bankCode the 3-digit CBN bank code
     * @return a valid, unique AccountNumber
     */
    public AccountNumber generate(BankCode bankCode) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            AccountNumber candidate = generateCandidate(bankCode);

            if (!accountRepository.existsByAccountNumber(candidate)) {
                log.debug("Generated NUBAN={} on attempt {}",
                        candidate.masked(), attempt);
                return candidate;
            }
            log.warn("NUBAN collision attempt {} — regenerating", attempt);
        }
        throw new BankException(
                ErrorCode.NUBAN_GENERATION_FAILED,
                "Failed to generate unique NUBAN after "
                        + MAX_RETRIES + " attempts"
        );
    }

    // Package-private so NubanGeneratorTest can test the algorithm directly
    AccountNumber generateCandidate(BankCode bankCode) {
        // Step 1: generate 9-digit random serial
        int[] serial = new int[9];
        for (int i = 0; i < 9; i++) {
            serial[i] = RANDOM.nextInt(10);
        }
        // Step 2: build the 12-digit input array
        // [bankCode digit 0, bankCode digit 1, bankCode digit 2,
        //  serial digit 0 ... serial digit 8]
        int[] input = new int[12];
        String code = bankCode.getValue();
        for (int i = 0; i < 3; i++) {
            input[i] = Character.getNumericValue(code.charAt(i));
        }
        System.arraycopy(serial, 0, input, 3, 9);

        // Step 3: weighted sum
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += input[i] * WEIGHTS[i];
        }

        // Step 4: check digit
        int checkDigit = (10 - (sum % 10)) % 10;

        // Step 5: NUBAN = 9-digit serial + check digit
        StringBuilder sb = new StringBuilder(10);
        for (int d : serial) sb.append(d);
        sb.append(checkDigit);

        return new AccountNumber(sb.toString());
    }

    /**
     * Validates a NUBAN for a given bank code.
     * Used when verifying account numbers entered by users.
     *
     * @return true if the NUBAN is valid for this bank code
     */
    public boolean validate(String bankCode, String nuban) {
        if (nuban == null || nuban.length() != 10 ||
                !nuban.matches("\\d{10}")) {
            return false;
        }

        String serial = nuban.substring(0, 9);
        String combined = bankCode + serial;

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += Character.getNumericValue(combined.charAt(i)) * WEIGHTS[i];
        }

        int expectedCheck = (10 - (sum % 10)) % 10;
        int actualCheck   = Character.getNumericValue(nuban.charAt(9));

        return expectedCheck == actualCheck;
    }
}