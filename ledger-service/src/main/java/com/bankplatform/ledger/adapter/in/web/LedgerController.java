package com.bankplatform.ledger.adapter.in.web;

import com.bankplatform.ledger.domain.model.LedgerEntry;
import com.bankplatform.ledger.domain.port.in.GetLedgerUseCase;
import com.bankplatform.shared.web.ApiResponse;
import com.bankplatform.shared.web.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final GetLedgerUseCase getLedgerUseCase;

    /**
     * GET /api/v1/ledger/statement/{accountNumber}
     * Returns paginated transaction history for an account.
     *
     * Example: GET /api/v1/ledger/statement/0000014579
     *          ?from=2025-01-01T00:00:00Z
     *          &to=2025-01-31T23:59:59Z
     *          &page=0&size=20
     */
    @GetMapping("/statement/{accountNumber}")
    public ApiResponse<PageResponse<LedgerEntryView>> getStatement(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "2020-01-01T00:00:00Z")
            Instant from,
            @RequestParam(defaultValue = "#{T(java.time.Instant).now()}")
            Instant to,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var pageable = PageRequest.of(
                page, size, Sort.by(Sort.Direction.DESC, "createdAt")
        );

        var entries = getLedgerUseCase.getStatement(
                accountNumber, from, to, pageable
        );

        return ApiResponse.ok(PageResponse.of(entries, LedgerEntryView::from));
    }

    /**
     * GET /internal/ledger/balance/{accountNumber}
     * Returns the authoritative balance calculated from ledger entries.
     * Called by account-service for reconciliation.
     */
    @GetMapping("/internal/ledger/balance/{accountNumber}")
    public ApiResponse<BalanceView> getBalance(
            @PathVariable String accountNumber
    ) {
        long kobo = getLedgerUseCase.calculateBalanceKobo(accountNumber);
        return ApiResponse.ok(new BalanceView(
                accountNumber,
                kobo,
                BigDecimal.valueOf(kobo, 2)
        ));
    }

    // ── Lightweight view records (no separate file needed) ──

    record LedgerEntryView(
            String     id,
            String     reference,
            String     entryType,
            BigDecimal amountNaira,
            String     description,
            String     counterpart,
            Instant    createdAt
    ) {
        static LedgerEntryView from(LedgerEntry e) {
            return new LedgerEntryView(
                    e.id(),
                    e.transactionReference(),
                    e.entryType().name(),
                    e.amount().toMajorUnits(),
                    e.description(),
                    e.counterpartAccountNumber(),
                    e.createdAt()
            );
        }
    }

    record BalanceView(
            String     accountNumber,
            long       balanceKobo,
            BigDecimal balanceNaira
    ) {}
}