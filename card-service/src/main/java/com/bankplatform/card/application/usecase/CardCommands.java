package com.bankplatform.card.application.usecase;

public final class CardCommands {

    private CardCommands() {}

    public record IssueCardCommand(
            String linkedNuban,
            String ownerUserId,
            String cardNetwork,  // VERVE, VISA, MASTERCARD
            long   spendingLimitKobo  // 0 = no limit
    ) {}

    /**
     * Command to process a card transaction.
     * cvv and pin are verified then discarded — never stored.
     */
    public record CardTransactionCommand(
            String cardNumber,
            String cvv,          // verified via HMAC, then discarded
            String pin,          // BCrypt verified, then discarded
            String expiryMonth,  // MM format
            String expiryYear,   // YY format
            long   amountKobo,
            String merchantName,
            String idempotencyKey
    ) {}

    public record SetCardPinCommand(
            String cardId,
            String userId,
            String rawPin
    ) {}

    public record FreezeCardCommand(
            String cardId,
            String userId
    ) {}

    public record UnfreezeCardCommand(
            String cardId,
            String userId
    ) {}
}