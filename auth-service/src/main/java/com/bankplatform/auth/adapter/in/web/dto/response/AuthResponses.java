package com.bankplatform.auth.adapter.in.web.dto.response;

import java.time.Instant;
import java.util.List;

public final class AuthResponses {
    private AuthResponses(){}

    public record AuthTokenResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresIn,
            String userId,
            String email
    ){}

    public record RegisterResponse(
            String userId,
            String email,
            String status,
            Instant createdAt
    ){}

    public record UserProfileResponse(
            String       userId,
            String       email,
            String       phoneNumber,
            List<String> roles,
            String       status,
            boolean      pinSet,
            Instant      createdAt
    ){}
}
