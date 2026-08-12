package com.bankplatform.account.adapter.in.web;

import com.bankplatform.account.adapter.in.web.dto.request.AccountRequests.*;
import com.bankplatform.account.adapter.in.web.dto.response.AccountResponses.*;
import com.bankplatform.account.adapter.in.web.mapper.AccountWebMapper;
import com.bankplatform.account.application.usecase.AccountCommands;
import com.bankplatform.account.domain.port.in.*;
import com.bankplatform.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final OpenAccountUseCase openAccountUseCase;
    private final GetAccountUseCase  getAccountUseCase;
    private final DebitAccountUseCase  debitAccountUseCase;
    private final CreditAccountUseCase creditAccountUseCase;
    private final AccountWebMapper   mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AccountResponse> openAccount(
            @Valid @RequestBody OpenAccountRequest request,
            @AuthenticationPrincipal String authUserId
    ) {
        var account = openAccountUseCase.openAccount(
                mapper.toCommand(request, authUserId)
        );
        return ApiResponse.ok(
                mapper.toResponse(account),
                "Account opened successfully"
        );
    }

    /**
     * GET /api/v1/accounts/{accountNumber}
     * Gets a single account by NUBAN.
     */
    @GetMapping("/{accountNumber}")
    public ApiResponse<AccountResponse> getAccount(
            @PathVariable String accountNumber
    ) {
        var account = getAccountUseCase.getByAccountNumber(accountNumber);
        return ApiResponse.ok(mapper.toResponse(account));
    }

    /**
     * GET /api/v1/accounts/bvn/{bvn}
     * Gets all accounts for a BVN across all banks.
     * This is how a customer sees all their linked accounts.
     */
    @GetMapping("/bvn/{bvn}")
    public ApiResponse<List<AccountSummary>> getAccountsByBvn(
            @PathVariable String bvn
    ) {
        var accounts = getAccountUseCase.getByBvn(bvn);
        return ApiResponse.ok(mapper.toSummaryList(accounts));
    }

    /**
     * GET /internal/accounts/{accountNumber}/verify
     * Called by transfer-service to verify an account exists
     * and is active before initiating a transfer.
     * Not exposed through the API Gateway.
     */
    @GetMapping("/internal/accounts/{accountNumber}/verify")
    public ApiResponse<AccountResponse> verifyAccount(
            @PathVariable String accountNumber
    ) {
        var account = getAccountUseCase.getByAccountNumber(accountNumber);
        return ApiResponse.ok(mapper.toResponse(account));
    }

    /**
     * POST /internal/accounts/debit
     * Called by transfer-service and card-service.
     * Debits an account by the specified amount.
     * Not exposed through API Gateway.
     */
    @PostMapping("/internal/accounts/debit")
    public ApiResponse<Void> debitAccount(
            @RequestBody DebitCreditRequest request
    ) {
        debitAccountUseCase.debit(new AccountCommands.DebitCommand(
                request.accountNumber(),
                request.amountKobo(),
                request.currency(),
                request.reference()
        ));
        return ApiResponse.noContent("Account debited successfully");
    }

    /**
     * POST /internal/accounts/credit
     * Called by transfer-service to credit destination account.
     * Not exposed through API Gateway.
     */
    @PostMapping("/internal/accounts/credit")
    public ApiResponse<Void> creditAccount(
            @RequestBody DebitCreditRequest request
    ) {
        creditAccountUseCase.credit(new AccountCommands.CreditCommand(
                request.accountNumber(),
                request.amountKobo(),
                request.currency(),
                request.reference()
        ));
        return ApiResponse.noContent("Account credited successfully");
    }

    // Add this record at the bottom of the class
    record DebitCreditRequest(
            String accountNumber,
            long   amountKobo,
            String currency,
            String reference
    ) {}
}