package com.bankplatform.transfer.adapter.in.web.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public final class TransferResponses {

    private TransferResponses() {}

    public record TransferResponse(
            String     transferId,
            String     sourceAccountNumber,
            String     destinationAccountNumber,
            String     destinationBankCode,
            BigDecimal amountNaira,        // display only — stored as kobo
            String     currency,
            String     narration,
            String     status,
            String     failureReason,      // null if successful
            Instant    createdAt,
            Instant    completedAt
    ) {}
}