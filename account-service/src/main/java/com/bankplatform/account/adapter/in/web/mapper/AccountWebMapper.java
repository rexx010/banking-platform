package com.bankplatform.account.adapter.in.web.mapper;

import com.bankplatform.account.adapter.in.web.dto.request.AccountRequests.*;
import com.bankplatform.account.adapter.in.web.dto.response.AccountResponses.*;
import com.bankplatform.account.application.usecase.AccountCommands.*;
import com.bankplatform.account.domain.model.Account;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel       = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AccountWebMapper {

    // Request → Command
    @Mapping(target = "authUserId", source = "authUserId")
    @Mapping(target = "bvn",        source = "request.bvn")
    @Mapping(target = "bankCode",   source = "request.bankCode")
    @Mapping(target = "accountType",source = "request.accountType")
    @Mapping(target = "currency",   source = "request.currency")
    OpenAccountCommand toCommand(OpenAccountRequest request, String authUserId);

    // Domain → Response
    @Mapping(target = "accountId", source = "id")
    @Mapping(target = "accountNumber",
            expression = "java(account.getAccountNumber().getValue())")
    @Mapping(target = "bankCode",
            expression = "java(account.getBankCode().getValue())")
    @Mapping(target = "accountType",
            expression = "java(account.getAccountType().name())")
    @Mapping(target = "balanceNaira",
            expression = "java(account.getBalance().toMajorUnits())")
    @Mapping(target = "status",
            expression = "java(account.getStatus().name())")
    @Mapping(target = "ownerBvn",
            expression = "java(com.bankplatform.shared.logging.MaskingUtil.maskBvn(account.getOwnerBvn()))")
    AccountResponse toResponse(Account account);

    @Mapping(target = "accountNumber",
            expression = "java(account.getAccountNumber().getValue())")
    @Mapping(target = "bankCode",
            expression = "java(account.getBankCode().getValue())")
    @Mapping(target = "accountType",
            expression = "java(account.getAccountType().name())")
    @Mapping(target = "balanceNaira",
            expression = "java(account.getBalance().toMajorUnits())")
    @Mapping(target = "status",
            expression = "java(account.getStatus().name())")
    AccountSummary toSummary(Account account);

    List<AccountSummary> toSummaryList(List<Account> accounts);
}