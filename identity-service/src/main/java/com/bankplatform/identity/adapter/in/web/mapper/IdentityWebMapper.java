package com.bankplatform.identity.adapter.in.web.mapper;

import com.bankplatform.identity.adapter.in.web.dto.request.IdentityRequests.*;
import com.bankplatform.identity.adapter.in.web.dto.response.IdentityResponses.*;
import com.bankplatform.identity.application.usecase.IdentityCommands.*;
import com.bankplatform.identity.domain.model.Customer;
import org.mapstruct.*;

@Mapper(
        componentModel       = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface IdentityWebMapper {

    // Request + authUserId → Command
    @Mapping(target = "authUserId",    source = "authUserId")
    @Mapping(target = "firstName",     source = "request.firstName")
    @Mapping(target = "lastName",      source = "request.lastName")
    @Mapping(target = "middleName",    source = "request.middleName")
    @Mapping(target = "dateOfBirth",   source = "request.dateOfBirth")
    @Mapping(target = "nin",           source = "request.nin")
    @Mapping(target = "phoneNumber",   source = "request.phoneNumber")
    @Mapping(target = "email",         source = "request.email")
    @Mapping(target = "address",       source = "request.address")
    @Mapping(target = "stateOfOrigin", source = "request.stateOfOrigin")
    CreateBvnCommand toCommand(CreateBvnRequest request, String authUserId);

    // Customer domain → CustomerResponse
    @Mapping(target = "customerId",    source = "id")
    @Mapping(
            target     = "bvn",
            expression = "java(customer.getBvn().masked())"
    )
    @Mapping(
            target     = "fullName",
            expression = "java(customer.getFullName())"
    )
    @Mapping(
            target     = "kycStatus",
            expression = "java(customer.getKycStatus().name())"
    )
    @Mapping(
            target     = "documentCount",
            expression = "java(customer.getDocuments().size())"
    )
    CustomerResponse toResponse(Customer customer);

    // Customer domain → BvnVerificationResponse
    @Mapping(
            target     = "bvn",
            expression = "java(customer.getBvn().masked())"
    )
    @Mapping(target = "customerId",  source = "id")
    @Mapping(
            target     = "fullName",
            expression = "java(customer.getFullName())"
    )
    @Mapping(
            target     = "kycVerified",
            expression = "java(customer.isKycVerified())"
    )
    BvnVerificationResponse toBvnVerificationResponse(Customer customer);
}