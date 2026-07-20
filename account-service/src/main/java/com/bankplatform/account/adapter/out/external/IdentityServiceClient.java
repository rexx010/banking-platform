package com.bankplatform.account.adapter.out.external;

import com.bankplatform.account.domain.port.out.BvnVerificationPort;
import com.bankplatform.shared.exceptions.BankException;
import com.bankplatform.shared.exceptions.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Slf4j
@Component
public class IdentityServiceClient implements BvnVerificationPort {

    private final RestClient restClient;

    public IdentityServiceClient(
            @Value("${services.identity.base-url}") String baseUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public boolean isBvnVerified(String bvn) {
        try {
            var response = restClient.get()
                    .uri("/internal/identity/bvn/{bvn}", bvn)
                    .retrieve()
                    .body(Map.class);

            if (response == null) return false;

            @SuppressWarnings("unchecked")
            var data = (Map<String, Object>) response.get("data");
            if (data == null) return false;

            return Boolean.TRUE.equals(data.get("kycVerified"));

        } catch (RestClientException ex) {
            log.error("Failed to verify BVN with identity-service: {}",
                    ex.getMessage());
            throw new BankException(
                    ErrorCode.BVN_VERIFICATION_FAILED,
                    "BVN verification service unavailable"
            );
        }
    }

    @Override
    public String getCustomerName(String bvn) {
        try {
            var response = restClient.get()
                    .uri("/internal/identity/bvn/{bvn}", bvn)
                    .retrieve()
                    .body(Map.class);

            if (response == null) return "Unknown";

            @SuppressWarnings("unchecked")
            var data = (Map<String, Object>) response.get("data");
            if (data == null) return "Unknown";

            return (String) data.getOrDefault("fullName", "Unknown");

        } catch (RestClientException ex) {
            log.warn("Could not fetch customer name for BVN: {}",
                    ex.getMessage());
            return "Unknown";
        }
    }
}