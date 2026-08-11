package com.bankplatform.card.adapter.out.external;

import com.bankplatform.card.domain.port.out.CardTransactionPort;
import com.bankplatform.shared.exceptions.BankException;
import com.bankplatform.shared.exceptions.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component
public class TransferServiceClient implements CardTransactionPort {

    private final RestClient restClient;

    public TransferServiceClient(
            @Value("${services.account.base-url}") String baseUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public void debitAccount(
            String accountNumber, long amountKobo,
            String currency, String idempotencyKey
    ) {
        try {
            restClient.post()
                    .uri("/internal/accounts/debit")
                    .body(Map.of(
                            "accountNumber", accountNumber,
                            "amountKobo",    amountKobo,
                            "currency",      currency,
                            "reference",     "CARD_" + idempotencyKey
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException ex) {
            throw new BankException(
                    ErrorCode.TRANSFER_INSUFFICIENT_FUNDS,
                    "Card transaction debit failed: " + ex.getMessage());
        }
    }
}