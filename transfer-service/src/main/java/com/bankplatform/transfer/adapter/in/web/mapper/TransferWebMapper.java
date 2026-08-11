package com.bankplatform.transfer.adapter.in.web.mapper;

import com.bankplatform.transfer.adapter.in.web.dto.request.TransferRequests.*;
import com.bankplatform.transfer.adapter.in.web.dto.response.TransferResponses.*;
import com.bankplatform.transfer.application.usecase.TransferCommands.*;
import com.bankplatform.transfer.domain.model.Transfer;
import org.mapstruct.*;

import java.math.BigDecimal;

@Mapper(
        componentModel       = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TransferWebMapper {

    @Mapping(target = "initiatedByUserId", source = "userId")
    @Mapping(target = "idempotencyKey",
            source = "request.idempotencyKey")
    @Mapping(target = "sourceAccountNumber",
            source = "request.sourceAccountNumber")
    @Mapping(target = "destinationAccountNumber",
            source = "request.destinationAccountNumber")
    @Mapping(target = "destinationBankCode",
            source = "request.destinationBankCode")
    @Mapping(target = "amountKobo",
            source = "request.amountKobo")
    @Mapping(target = "currency",    source = "request.currency")
    @Mapping(target = "narration",   source = "request.narration")
    @Mapping(target = "transactionPin", source = "request.transactionPin")
    InitiateTransferCommand toCommand(
            InitiateTransferRequest request, String userId);

    @Mapping(target = "transferId", source = "id")
    @Mapping(target = "status",
            expression = "java(transfer.getStatus().name())")
    @Mapping(target = "amountNaira",
            expression = """
                 java(java.math.BigDecimal.valueOf(
                     transfer.getAmountKobo(), 2))
                 """)
    TransferResponse toResponse(Transfer transfer);
}