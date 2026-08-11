package com.bankplatform.transfer.adapter.in.web;

import com.bankplatform.transfer.adapter.in.web.dto.request.TransferRequests.*;
import com.bankplatform.transfer.adapter.in.web.dto.response.TransferResponses.*;
import com.bankplatform.transfer.adapter.in.web.mapper.TransferWebMapper;
import com.bankplatform.transfer.domain.model.Transfer;
import com.bankplatform.transfer.domain.port.in.*;
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
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final InitiateTransferUseCase initiateUseCase;
    private final GetTransferUseCase      getUseCase;
    private final TransferWebMapper       mapper;

    /**
     * POST /api/v1/transfers
     * Initiates a money transfer.
     *
     * The request body includes the transaction PIN.
     * The use case verifies the PIN against auth-service
     * before debiting any account.
     *
     * Returns 201 Created with transfer details and status.
     * Status may be COMPLETED (fast path) or PROCESSING
     * (if async processing is needed).
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TransferResponse> initiateTransfer(
            @Valid @RequestBody InitiateTransferRequest request,
            @AuthenticationPrincipal String userId
    ) {
        Transfer transfer = initiateUseCase.initiate(
                mapper.toCommand(request, userId)
        );
        return ApiResponse.ok(
                mapper.toResponse(transfer),
                "Transfer " + transfer.getStatus().name().toLowerCase()
        );
    }

    /**
     * GET /api/v1/transfers/{transferId}
     * Gets transfer status. Use this to poll after initiating.
     */
    @GetMapping("/{transferId}")
    public ApiResponse<TransferResponse> getTransfer(
            @PathVariable String transferId
    ) {
        return ApiResponse.ok(
                mapper.toResponse(getUseCase.getById(transferId))
        );
    }

    /**
     * GET /api/v1/transfers/account/{accountNumber}
     * Gets all transfers initiated from an account.
     */
    @GetMapping("/account/{accountNumber}")
    public ApiResponse<List<TransferResponse>> getByAccount(
            @PathVariable String accountNumber
    ) {
        List<TransferResponse> transfers = getUseCase
                .getByAccountNumber(accountNumber)
                .stream()
                .map(mapper::toResponse)
                .toList();
        return ApiResponse.ok(transfers);
    }
}