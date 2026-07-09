package com.bankplatform.auth.adapter.in.web.mapper;

import com.bankplatform.auth.adapter.in.web.dto.request.AuthRequests.*;
import com.bankplatform.auth.adapter.in.web.dto.response.AuthResponses.*;
import com.bankplatform.auth.application.usecase.AuthCommands.*;
import com.bankplatform.auth.domain.model.Role;
import com.bankplatform.auth.domain.model.User;
import com.bankplatform.auth.domain.model.UserStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface AuthWebMapper {
//    Request -> Command
    @Mapping(source = "password", target = "rawPassword")
    RegisterUserCommand toCommand(RegisterRequest request);

    @Mapping(source = "password", target = "rawPassword")
    @Mapping(target = "deviceId", ignore = true)
    LoginCommand toCommand(LoginRequest request);

//     Domain → Response
    @Mapping(source = "id",     target = "userId")
    @Mapping(source = "status", target = "status",
            qualifiedByName    = "statusToString")
    RegisterResponse toRegisterResponse(User user);

    @Mapping(source = "id",          target = "userId")
    @Mapping(source = "status",      target = "status",
            qualifiedByName         = "statusToString")
    @Mapping(source = "roles",       target = "roles",
            qualifiedByName         = "rolesToStrings")
    @Mapping(target = "pinSet",
            expression             = "java(user.hasPinSet())")
    UserProfileResponse toProfileResponse(User user);

//    TokenPair → AuthTokenResponse
    @Mapping(source = "accessTokenExpiresInSeconds", target = "expiresIn")
    @Mapping(target = "tokenType", constant = "Bearer")
    AuthTokenResponse toTokenResponse(TokenPair pair);

//    Converters
    @Named("statusToString")
    default String statusToString(UserStatus status) {
        return status == null ? null : status.name();
    }

    @Named("rolesToStrings")
    default List<String> rolesToStrings(Set<Role> roles) {
        if (roles == null) return List.of();
        return roles.stream()
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}
