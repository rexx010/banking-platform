package com.bankplatform.ledger.application.usecase;

public final class LedgerCommands {

    private LedgerCommands() {}

    public record RecordTransactionCommand(
            String reference,
            String sourceAccountNumber,
            String destAccountNumber,
            long   amountKobo,
            String currency,
            String description
    ) {}
}