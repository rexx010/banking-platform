package com.bankplatform.card.adapter.in.web;

import com.bankplatform.card.application.usecase.CardApplicationService;
import com.bankplatform.card.application.usecase.CardCommands.*;
import com.bankplatform.card.domain.model.Card;
import com.bankplatform.card.domain.port.in.*;
import com.bankplatform.shared.web.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final IssueCardUseCase              issueCardUseCase;
    private final GetCardUseCase                getCardUseCase;
    private final ProcessCardTransactionUseCase transactionUseCase;
    private final CardApplicationService        cardService;

    /** POST /api/v1/cards — issues a new card */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CardView> issueCard(
            @Valid @RequestBody IssueRequest request,
            @AuthenticationPrincipal String userId
    ) {
        Card card = issueCardUseCase.issueCard(new IssueCardCommand(
                request.linkedNuban(), userId,
                request.cardNetwork(), request.spendingLimitKobo()
        ));
        return ApiResponse.ok(CardView.from(card, null),
                "Card issued successfully");
    }

    /** GET /api/v1/cards — lists all cards for the authenticated user */
    @GetMapping
    public ApiResponse<List<CardView>> myCards(
            @AuthenticationPrincipal String userId
    ) {
        var cards = getCardUseCase.getByOwnerUserId(userId)
                .stream().map(c -> CardView.from(c, null)).toList();
        return ApiResponse.ok(cards);
    }

    /** POST /api/v1/cards/transact — processes a card payment */
    @PostMapping("/transact")
    public ApiResponse<Void> processTransaction(
            @Valid @RequestBody TransactRequest request
    ) {
        transactionUseCase.processTransaction(new CardTransactionCommand(
                request.cardNumber(), request.cvv(), request.pin(),
                request.expiryMonth(), request.expiryYear(),
                request.amountKobo(), request.merchantName(),
                request.idempotencyKey()
        ));
        return ApiResponse.noContent("Transaction approved");
    }

    /** POST /api/v1/cards/{cardId}/pin — sets card PIN */
    @PostMapping("/{cardId}/pin")
    public ApiResponse<Void> setPin(
            @PathVariable String cardId,
            @RequestBody SetPinRequest request,
            @AuthenticationPrincipal String userId
    ) {
        cardService.setPin(new SetCardPinCommand(
                cardId, userId, request.pin()));
        return ApiResponse.noContent("Card PIN set successfully");
    }

    /** POST /api/v1/cards/{cardId}/freeze */
    @PostMapping("/{cardId}/freeze")
    public ApiResponse<Void> freeze(
            @PathVariable String cardId,
            @AuthenticationPrincipal String userId
    ) {
        cardService.freeze(new FreezeCardCommand(cardId, userId));
        return ApiResponse.noContent("Card frozen");
    }

    /** POST /api/v1/cards/{cardId}/unfreeze */
    @PostMapping("/{cardId}/unfreeze")
    public ApiResponse<Void> unfreeze(
            @PathVariable String cardId,
            @AuthenticationPrincipal String userId
    ) {
        cardService.unfreeze(new UnfreezeCardCommand(cardId, userId));
        return ApiResponse.noContent("Card unfrozen");
    }

    // ── Inline request/response records ──────────────────

    record IssueRequest(
            @NotBlank String linkedNuban,
            @NotBlank String cardNetwork,
            long spendingLimitKobo
    ) {}

    record TransactRequest(
            @NotBlank @Pattern(regexp = "\\d{16}") String cardNumber,
            @NotBlank @Pattern(regexp = "\\d{3}")  String cvv,
            @NotBlank @Pattern(regexp = "\\d{4,6}")String pin,
            @NotBlank @Pattern(regexp = "\\d{2}")  String expiryMonth,
            @NotBlank @Pattern(regexp = "\\d{2}")  String expiryYear,
            @Positive long amountKobo,
            @NotBlank String merchantName,
            @NotBlank String idempotencyKey
    ) {}

    record SetPinRequest(
            @NotBlank @Pattern(regexp = "\\d{4,6}") String pin
    ) {}

    record CardView(
            String cardId,
            String maskedNumber,
            String network,
            String expiryMonth,
            String expiryYear,
            String status,
            String cvv            // only populated at issuance — null otherwise
    ) {
        static CardView from(Card card, String cvv) {
            return new CardView(
                    card.getId(), card.getMaskedNumber(),
                    card.getCardNetwork().name(),
                    String.format("%02d",
                            card.getExpiryDate().getMonthValue()),
                    String.valueOf(card.getExpiryDate().getYear()),
                    card.getStatus().name(), cvv
            );
        }
    }
}