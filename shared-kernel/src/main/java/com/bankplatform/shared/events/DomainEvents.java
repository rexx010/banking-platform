package com.bankplatform.shared.events;

import java.time.Instant;

public final class DomainEvents {
    private DomainEvents(){}

    /**ACCOUNT EVENTS*/
    public record AccountCreatedEvent(
            String eventId,
            Instant occurredAt,
            String traceId,
            String accountId,
            String nuban,
            String bankCode,
            String ownerBvn,
            String accountType,
            String currency
    ){}

    public record AccountStatusChangedEvent(
            String eventId,
            Instant occurredAt,
            String traceId,
            String accountId,
            String nuban,
            String previousState,
            String newStatus,
            String reason
    ){}

    /** IDENTITY EVENT*/
    public record BvnCreatedEvent(
            String eventId,
            Instant occurredAt,
            String traceId,
            String bvn,
            String customerId,
            String fullname
    ){}

    public record KycVerifiedEvent(
            String eventId,
            Instant occurredAt,
            String traceId,
            String bvn,
            String customerId,
            String verifiedBy
    ){}

    /**TRANSFER EVENTS*/
    public record TransferInitiatedEVENT(
            String eventId,
            Instant occurredAt,
            String traceId,
            String transferId,
            String idempotencyKey,
            String sourceNuban,
            String destinationNuban,
            String destinationBankCode,
            long amountKobo,
            String currency,
            String narration
    ){}

    public record TransferCompletedEvent(
            String eventId,
            Instant occurredAt,
            String traceId,
            String transferId,
            String sourceNuban,
            String destinationNuban,
            long amountKobo,
            String currency,
            String narration
    ){}

    public record TransferFailedEvent(
            String  eventId,
            Instant occurredAt,
            String  traceId,
            String  transferId,
            String  sourceNuban,
            String  destinationNuban,
            long    amountKobo,
            String  failureReason,
            String  errorCode
    ){}

    public record TransferReversedEvent(
            String  eventId,
            Instant occurredAt,
            String  traceId,
            String  originalTransferId,
            String  sourceNuban,
            long    amountKobo,
            String  reversalReason
    ) {}

    /**CARD EVENT*/
    public record CardIssuedEvent(
            String eventId,
            Instant occurredAt,
            String traceId,
            String cardId,
            String maskedCardNumber,
            String linkedNuban,
            String expiryMonth,
            String expiryYear,
            String cardNetwork
    ){}

    public record CardTransactionEvent(
            String eventId,
            Instant occurredAt,
            String traceId,
            String cardId,
            String maskedCardNumber,
            String linkedNuban,
            long amountKobo,
            String merchantName,
            String status,
            String declineReason
    ){}

    /**NOTIFICATION EVENT*/
    public record NotificationRequestedEvent(
            String eventId,
            Instant occurredAt,
            String traceId,
            String recipientId,
            String channel,
            String templateKey,
            String subject,
            String message,
            String phoneNumber,
            String email
    ){}
}
