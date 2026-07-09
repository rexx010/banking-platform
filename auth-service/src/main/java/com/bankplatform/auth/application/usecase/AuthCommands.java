package com.bankplatform.auth.application.usecase;

public final class AuthCommands {
    private AuthCommands() {}

    public record RegisterUserCommand(
            String email,
            String rawPassword,
            String phoneNumber
    ){}

    /**
     Input for the login use case.
     deviceId is optional — used to detect logins from
     unrecognised devices so we can alert the user.
     */
    public record LoginCommand(
            String email,
            String rawPassword,
            String deviceId
    ){}

    /**
     Result returned by both login and refresh.
     Contains everything the mobile app needs to
     start making authenticated API calls.

     accessToken  — include in every request header:
                     Authorization: Bearer {accessToken}

     refreshToken — store securely, use only to get
                     a new accessToken when it expires.
                     Never send to any other endpoint.
     */
    public record TokenPair(
            String accessToken,
            String refreshToken,
            long accessTokenExpiresInSeconds,
            String userId,
            String email
    ){}

    /**
     * Input for updating a user's profile.
     * Only non-sensitive fields can be changed here.
     * Password change requires a separate dedicated flow
     * with current password confirmation.
     */
    public record UpdateProfileCommand(
            String phoneNumber
    ) {}
}
