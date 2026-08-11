package com.bankplatform.card.domain.model;

import com.bankplatform.shared.exceptions.BankException;
import com.bankplatform.shared.exceptions.ErrorCode;
import com.bankplatform.shared.util.IdGenerator;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.Instant;

/**
 * Card domain entity.
 *
 * Key design decisions:
 * - CVV is NEVER stored — computed on demand from card number + expiry + secret
 * - Card number is stored (customers need to see it)
 * - PIN is stored as BCrypt hash (same as account PIN)
 * - Expiry is a YearMonth — cards expire at end of month, not a specific day
 */
public class Card {

    private final String      id;
    private final String      cardNumber;     // 16-digit Luhn-valid number
    private final String      linkedNuban;    // account this card debits
    private final String      ownerUserId;
    private final CardNetwork cardNetwork;
    private final YearMonth   expiryDate;     // expires end of this month
    private       CardStatus  status;
    private       String      cardPinHash;    // BCrypt hash, never plain text
    private       long        spendingLimitKobo;
    private final Instant     issuedAt;
    private       Instant     updatedAt;

    // ── Static factory ────────────────────────────────────

    public static Card issue(
            String      cardNumber,
            String      linkedNuban,
            String      ownerUserId,
            CardNetwork cardNetwork,
            YearMonth   expiryDate,
            long        spendingLimitKobo
    ) {
        return new Card(
                IdGenerator.generate(),
                cardNumber, linkedNuban, ownerUserId,
                cardNetwork, expiryDate,
                CardStatus.ACTIVE, null,
                spendingLimitKobo, Instant.now()
        );
    }

    private Card(
            String id, String cardNumber, String linkedNuban,
            String ownerUserId, CardNetwork cardNetwork,
            YearMonth expiryDate, CardStatus status,
            String cardPinHash, long spendingLimitKobo, Instant issuedAt
    ) {
        this.id                = id;
        this.cardNumber        = cardNumber;
        this.linkedNuban       = linkedNuban;
        this.ownerUserId       = ownerUserId;
        this.cardNetwork       = cardNetwork;
        this.expiryDate        = expiryDate;
        this.status            = status;
        this.cardPinHash       = cardPinHash;
        this.spendingLimitKobo = spendingLimitKobo;
        this.issuedAt          = issuedAt;
        this.updatedAt         = issuedAt;
    }

    // ── Domain behaviour ──────────────────────────────────

    /**
     * Validates that this card can be used for a transaction.
     * Called before processing any card payment.
     */
    public void assertCanTransact(long amountKobo) {
        if (status == CardStatus.BLOCKED) {
            throw new BankException(ErrorCode.CARD_BLOCKED);
        }
        if (status == CardStatus.FROZEN) {
            throw new BankException(ErrorCode.CARD_BLOCKED,
                    "Card is frozen — unfreeze it first");
        }
        if (isExpired()) {
            throw new BankException(ErrorCode.CARD_EXPIRED);
        }
        if (spendingLimitKobo > 0 && amountKobo > spendingLimitKobo) {
            throw new BankException(ErrorCode.CARD_SPENDING_LIMIT,
                    "Transaction exceeds card spending limit");
        }
    }

    public boolean isExpired() {
        return YearMonth.now().isAfter(expiryDate);
    }

    public void freeze() {
        if (status != CardStatus.ACTIVE) {
            throw new BankException(ErrorCode.CARD_BLOCKED,
                    "Can only freeze an active card");
        }
        this.status    = CardStatus.FROZEN;
        this.updatedAt = Instant.now();
    }

    public void unfreeze() {
        if (status != CardStatus.FROZEN) {
            throw new BankException(ErrorCode.VALIDATION_FAILED,
                    "Card is not frozen");
        }
        this.status    = CardStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void block() {
        this.status    = CardStatus.BLOCKED;
        this.updatedAt = Instant.now();
    }

    public void setPin(String pinHash) {
        this.cardPinHash = pinHash;
        this.updatedAt   = Instant.now();
    }

    public void setSpendingLimit(long limitKobo) {
        this.spendingLimitKobo = limitKobo;
        this.updatedAt         = Instant.now();
    }

    /** Returns only last 4 digits — safe for display and logging */
    public String getMaskedNumber() {
        return "****" + cardNumber.substring(12);
    }

    // ── Getters ───────────────────────────────────────────

    public String      getId()               { return id; }
    public String      getCardNumber()       { return cardNumber; }
    public String      getLinkedNuban()      { return linkedNuban; }
    public String      getOwnerUserId()      { return ownerUserId; }
    public CardNetwork getCardNetwork()      { return cardNetwork; }
    public YearMonth   getExpiryDate()       { return expiryDate; }
    public CardStatus  getStatus()           { return status; }
    public String      getCardPinHash()      { return cardPinHash; }
    public long        getSpendingLimitKobo(){ return spendingLimitKobo; }
    public Instant     getIssuedAt()         { return issuedAt; }
    public Instant     getUpdatedAt()        { return updatedAt; }
}