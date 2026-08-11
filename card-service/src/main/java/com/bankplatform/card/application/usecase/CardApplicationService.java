package com.bankplatform.card.application.usecase;

import com.bankplatform.card.application.usecase.CardCommands.*;
import com.bankplatform.card.domain.model.*;
import com.bankplatform.card.domain.port.in.*;
import com.bankplatform.card.domain.port.out.*;
import com.bankplatform.shared.exceptions.BankException;
import com.bankplatform.shared.exceptions.ErrorCode;
import com.bankplatform.shared.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CardApplicationService
        implements IssueCardUseCase, GetCardUseCase,
        ProcessCardTransactionUseCase {

    private final CardRepository    cardRepository;
    private final CardEventPublisher eventPublisher;
    private final CardGenerator     cardGenerator;
    private final CardTransactionPort transactionPort;
    private final PasswordEncoder   passwordEncoder;

    // ── Issue Card ────────────────────────────────────────

    @Override
    public Card issueCard(IssueCardCommand command) {
        log.info("Issuing {} card for nuban={}",
                command.cardNetwork(), command.linkedNuban());

        // One active card per account per network
        if (cardRepository.existsByLinkedNuban(command.linkedNuban())) {
            throw new BankException(ErrorCode.CARD_ALREADY_ISSUED,
                    "A card already exists for this account");
        }

        CardNetwork network = CardNetwork.valueOf(command.cardNetwork());
        String cardNumber   = cardGenerator.generateCardNumber(network);

        // Cards expire 3 years from issuance
        YearMonth expiry = YearMonth.now().plusYears(3);

        Card card = Card.issue(
                cardNumber, command.linkedNuban(),
                command.ownerUserId(), network, expiry,
                command.spendingLimitKobo()
        );

        Card saved = cardRepository.save(card);
        eventPublisher.publishCardIssued(saved);

        log.info("Card issued cardId={} masked={}",
                saved.getId(), saved.getMaskedNumber());

        return saved;
    }

    // ── Process Transaction ───────────────────────────────

    @Override
    public Card processTransaction(CardTransactionCommand command) {
        // Find card by number
        Card card = cardRepository
                .findByCardNumber(command.cardNumber())
                .orElseThrow(() ->
                        new BankException(ErrorCode.CARD_NOT_FOUND));

        String eventId = IdGenerator.generate();

        try {
            // 1. Validate card status, expiry, spending limit
            card.assertCanTransact(command.amountKobo());

            // 2. Verify expiry matches what was provided
            YearMonth expiry = YearMonth.of(
                    2000 + Integer.parseInt(command.expiryYear()),
                    Integer.parseInt(command.expiryMonth())
            );
            if (!expiry.equals(card.getExpiryDate())) {
                throw new BankException(ErrorCode.CARD_EXPIRED,
                        "Card expiry does not match");
            }

            // 3. Verify CVV — recomputed, never stored
            if (!cardGenerator.verifyCvv(
                    card.getCardNumber(), card.getExpiryDate(),
                    command.cvv())) {
                throw new BankException(ErrorCode.CARD_INVALID_CVV);
            }

            // 4. Verify card PIN
            if (card.getCardPinHash() == null) {
                throw new BankException(ErrorCode.AUTH_PIN_NOT_SET,
                        "Card PIN has not been set");
            }
            if (!passwordEncoder.matches(command.pin(), card.getCardPinHash())) {
                throw new BankException(ErrorCode.CARD_PIN_INVALID);
            }

            // 5. Debit the linked account via transfer-service
            transactionPort.debitAccount(
                    card.getLinkedNuban(),
                    command.amountKobo(), "NGN",
                    command.idempotencyKey()
            );

            // 6. Publish approved event
            eventPublisher.publishTransactionApproved(
                    card, command.amountKobo(),
                    command.merchantName(), eventId);

            log.info("Card transaction approved cardId={} amount={}",
                    card.getId(), command.amountKobo());

        } catch (BankException ex) {
            // Publish declined event for all known failures
            eventPublisher.publishTransactionDeclined(
                    card, command.amountKobo(),
                    command.merchantName(), ex.getMessage(), eventId);
            throw ex;
        }

        return card;
    }

    // ── Read operations ───────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Card getById(String cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new BankException(ErrorCode.CARD_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Card> getByOwnerUserId(String userId) {
        return cardRepository.findByOwnerUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Card getByLinkedNuban(String accountNumber) {
        return cardRepository.findByLinkedNuban(accountNumber)
                .orElseThrow(() -> new BankException(ErrorCode.CARD_NOT_FOUND));
    }

    // ── Card management ───────────────────────────────────

    public Card setPin(SetCardPinCommand command) {
        Card card = cardRepository.findById(command.cardId())
                .orElseThrow(() -> new BankException(ErrorCode.CARD_NOT_FOUND));

        if (!card.getOwnerUserId().equals(command.userId())) {
            throw new BankException(ErrorCode.FORBIDDEN);
        }

        card.setPin(passwordEncoder.encode(command.rawPin()));
        return cardRepository.save(card);
    }

    public Card freeze(FreezeCardCommand command) {
        Card card = cardRepository.findById(command.cardId())
                .orElseThrow(() -> new BankException(ErrorCode.CARD_NOT_FOUND));

        card.freeze();
        return cardRepository.save(card);
    }

    public Card unfreeze(UnfreezeCardCommand command) {
        Card card = cardRepository.findById(command.cardId())
                .orElseThrow(() -> new BankException(ErrorCode.CARD_NOT_FOUND));

        card.unfreeze();
        return cardRepository.save(card);
    }
}