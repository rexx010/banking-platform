package com.bankplatform.account.application.usecase;

public final class AccountCommands {

    private AccountCommands() {}

    public record OpenAccountCommand(
            String authUserId,   // from JWT — who is opening the account
            String bvn,          // must be verified in identity-service
            String bankCode,     // 3-digit CBN bank code e.g. "058"
            String accountType,  // SAVINGS, CURRENT, DOMICILIARY
            String currency      // NGN, USD, GBP
    ) {}

    public record DebitCommand(
            String accountNumber,
            long   amountKobo,
            String currency,
            String reference     // transfer ID, card transaction ID etc.
    ) {}

    public record CreditCommand(
            String accountNumber,
            long   amountKobo,
            String currency,
            String reference
    ) {}
}