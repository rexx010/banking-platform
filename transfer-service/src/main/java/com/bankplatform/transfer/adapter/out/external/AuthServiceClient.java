package com.bankplatform.transfer.adapter.out.external;

import com.bankplatform.shared.exceptions.BankException;
import com.bankplatform.shared.exceptions.ErrorCode;
import com.bankplatform.transfer.domain.port.out.PinVerificationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Calls auth-service to verify a transaction PIN.
 *
 * Uses Spring's RestClient (introduced in Spring 6).
 * Throws BankException so the caller gets a clean error —
 * not a raw HTTP exception.
 */
@Slf4j
@Component
public class AuthServiceClient implements PinVerificationPort {

    private final RestClient restClient;

    public AuthServiceClient(
            @Value("${services.auth.base-url}") String baseUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public void verifyOrThrow(String userId, String rawPin) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri("/internal/auth/pin/verify")
                    .body(Map.of("userId", userId, "pin", rawPin))
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                throw new BankException(ErrorCode.AUTH_PIN_INVALID,
                        "PIN verification returned no response");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data =
                    (Map<String, Object>) response.get("data");

            boolean valid = data != null &&
                    Boolean.TRUE.equals(data.get("valid"));

            if (!valid) {
                throw new BankException(ErrorCode.AUTH_PIN_INVALID,
                        "Transaction PIN is incorrect");
            }

        } catch (BankException ex) {
            // Re-throw BankExceptions as-is
            throw ex;
        } catch (RestClientException ex) {
            log.error("PIN verification service unavailable: {}",
                    ex.getMessage());
            throw new BankException(ErrorCode.INTERNAL_ERROR,
                    "PIN verification service is unavailable");
        }
    }
}