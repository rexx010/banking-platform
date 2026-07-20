package com.bankplatform.account.adapter.in.web.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public final class AccountResponses {

    private AccountResponses() {}

    public record AccountResponse(
            String     accountId,
            String     accountNumber,   // full NUBAN e.g. "0000014579"
            String     bankCode,
            String     accountType,
            String     currency,
            BigDecimal balanceNaira,    // display only — stored internally as kobo
            String     status,
            String     ownerBvn,        // masked
            Instant    createdAt
    ) {}

    public record AccountSummary(
            String     accountNumber,
            String     bankCode,
            String     accountType,
            String     currency,
            BigDecimal balanceNaira,
            String     status
    ) {}
}