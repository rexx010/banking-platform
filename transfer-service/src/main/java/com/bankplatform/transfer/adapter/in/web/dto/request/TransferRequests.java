package com.bankplatform.transfer.adapter.in.web.dto.request;

import jakarta.validation.constraints.*;

public final class TransferRequests {

    private TransferRequests() {}

    public record InitiateTransferRequest(

            @NotBlank(message = "Idempotency key is required")
            String idempotencyKey,

            @NotBlank(message = "Source account number is required")
            @Pattern(regexp = "\\d{10}",
                    message = "Source account must be a valid 10-digit NUBAN")
            String sourceAccountNumber,

            @NotBlank(message = "Destination account number is required")
            @Pattern(regexp = "\\d{10}",
                    message = "Destination account must be a valid 10-digit NUBAN")
            String destinationAccountNumber,

            @NotBlank(message = "Destination bank code is required")
            @Pattern(regexp = "\\d{3}",
                    message = "Bank code must be 3 digits")
            String destinationBankCode,

            @Positive(message = "Amount must be greater than zero")
            long amountKobo,

            @NotBlank(message = "Currency is required")
            @Pattern(regexp = "[A-Z]{3}",
                    message = "Currency must be a 3-letter ISO code")
            String currency,

            @Size(max = 200,
                    message = "Narration cannot exceed 200 characters")
            String narration,

            @NotBlank(message = "Transaction PIN is required")
            @Pattern(regexp = "\\d{4,6}",
                    message = "PIN must be 4 to 6 digits")
            String transactionPin

    ) {}
}