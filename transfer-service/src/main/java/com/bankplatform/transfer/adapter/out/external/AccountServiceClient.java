package com.bankplatform.transfer.adapter.out.external;

import com.bankplatform.shared.exceptions.BankException;
import com.bankplatform.shared.exceptions.ErrorCode;
import com.bankplatform.transfer.domain.port.out.AccountOperationsPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Calls account-service to perform debit and credit operations.
 *
 * account-service owns account data — we never touch its database directly.
 * All account operations go through its REST API.
 *
 * Error handling:
 *   4xx errors (INSUFFICIENT_FUNDS, ACCOUNT_SUSPENDED etc.)
 *   are converted to BankException so the SAGA can handle them properly.
 *   5xx errors (account-service is down) propagate as RuntimeException
 *   triggering SAGA compensation.
 */
@Slf4j
@Component
public class AccountServiceClient implements AccountOperationsPort {

    private final RestClient restClient;

    public AccountServiceClient(
            @Value("${services.account.base-url}") String baseUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public void debit(
            String accountNumber, long amountKobo,
            String currency, String reference
    ) {
        log.info("Debiting account={} amount={} ref={}",
                accountNumber, amountKobo, reference);
        try {
            restClient.post()
                    .uri("/internal/accounts/debit")
                    .body(Map.of(
                            "accountNumber", accountNumber,
                            "amountKobo",    amountKobo,
                            "currency",      currency,
                            "reference",     reference
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException ex) {
            throw new BankException(
                    ErrorCode.TRANSFER_INSUFFICIENT_FUNDS,
                    "Debit failed: " + ex.getMessage());
        }
    }

    @Override
    public void credit(
            String accountNumber, long amountKobo,
            String currency, String reference
    ) {
        log.info("Crediting account={} amount={} ref={}",
                accountNumber, amountKobo, reference);
        try {
            restClient.post()
                    .uri("/internal/accounts/credit")
                    .body(Map.of(
                            "accountNumber", accountNumber,
                            "amountKobo",    amountKobo,
                            "currency",      currency,
                            "reference",     reference
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException ex) {
            throw new BankException(
                    ErrorCode.TRANSFER_FAILED,
                    "Credit failed: " + ex.getMessage());
        }
    }

    @Override
    public boolean isAccountActive(String accountNumber) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.get()
                    .uri("/internal/accounts/{number}/verify", accountNumber)
                    .retrieve()
                    .body(Map.class);

            if (response == null) return false;
            @SuppressWarnings("unchecked")
            Map<String, Object> data =
                    (Map<String, Object>) response.get("data");
            return data != null &&
                    "ACTIVE".equals(data.get("status"));
        } catch (Exception ex) {
            log.warn("Could not verify account {}: {}",
                    accountNumber, ex.getMessage());
            return false;
        }
    }
}