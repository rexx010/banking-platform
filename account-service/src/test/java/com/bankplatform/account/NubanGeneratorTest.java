package com.bankplatform.account;

import com.bankplatform.account.application.usecase.NubanGenerator;
import com.bankplatform.account.domain.model.AccountNumber;
import com.bankplatform.account.domain.model.BankCode;
import com.bankplatform.account.domain.port.out.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NUBAN Generator")
class NubanGeneratorTest {

    @Mock  AccountRepository  accountRepository;
    @InjectMocks NubanGenerator generator;

    @Test
    @DisplayName("Validates CBN document Example 1 — First Bank 0000014579")
    void validate_cbnExample1() {
        // The algorithm: bank=011, serial=000001457
        // 0*3+1*7+1*3+0*3+0*7+0*3+0*3+0*7+1*3+4*3+5*7+7*3 = 81
        // 81 % 10 = 1, check = 10 - 1 = 9
        // NUBAN = 0000014579

        boolean valid = generator.validate("011", "0000014579");

        assertThat(valid).isTrue();
    }

    /**
     * CBN document Example 2:
     * Bank: First Bank (011), Serial: 000000022
     * Expected NUBAN: 0000000220 (check digit = 0)
     */
    @Test
    @DisplayName("Validates CBN document Example 2 — check digit zero edge case")
    void validate_cbnExample2() {
        // 0*3+1*7+1*3+0*3+0*7+0*3+0*3+0*7+0*3+0*3+2*7+2*3 = 30
        // 30 % 10 = 0, check = (10 - 0) % 10 = 0
        // NUBAN = 0000000220

        boolean valid = generator.validate("011", "0000000220");

        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("Rejects NUBAN with wrong check digit")
    void validate_rejectsWrongCheckDigit() {
        // Valid NUBAN is 0000014579 — change last digit to 8
        boolean valid = generator.validate("011", "0000014578");

        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("Rejects NUBAN that is not 10 digits")
    void validate_rejectsWrongLength() {
        assertThat(generator.validate("011", "12345")).isFalse();
        assertThat(generator.validate("011", "123456789012")).isFalse();
    }

//    @RepeatedTest(50)
//    @DisplayName("Generated NUBAN always passes its own validation")
//    void generated_alwaysPassesValidation() {
//        when(accountRepository.existsByAccountNumber(any()))
//                .thenReturn(false);
//
//        AccountNumber nuban = generator.generateCandidate(new BankCode("058"));
//
//        assertThat(nuban.getValue()).hasSize(10);
//        assertThat(nuban.getValue()).matches("\\d{10}");
//        assertThat(generator.validate("058", nuban.getValue())).isTrue();
//    }

    @Test
    @DisplayName("Retries on collision and eventually succeeds")
    void generate_retriesOnCollision() {
        when(accountRepository.existsByAccountNumber(any()))
                .thenReturn(true)   // first attempt: collision
                .thenReturn(true)   // second attempt: collision
                .thenReturn(false); // third attempt: unique

        AccountNumber result = generator.generate(new BankCode("011"));

        assertThat(result).isNotNull();
        assertThat(result.getValue()).hasSize(10);
    }
}