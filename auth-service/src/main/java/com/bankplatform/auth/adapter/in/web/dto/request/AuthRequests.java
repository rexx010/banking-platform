package com.bankplatform.auth.adapter.in.web.dto.request;

import jakarta.validation.constraints.*;

public final class AuthRequests {
    private AuthRequests(){}

    public record RegisterRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Must be a valid email address")
            String email,

            @NotBlank(message = "Password is required")
            @Size(
                    min = 8, max = 15,
                    message = "Password must be between 8 and 15 characters"
            )
            @Pattern(
                    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
                    message = "Password must contain at least one uppercase " +
                            "letter, one lowercase letter, and one digit"
            )
            String password,

            @NotBlank(message = "Phone number is required")
            @Pattern(
                    regexp = "^(\\+234|0)[789][01]\\d{8}$",
                    message = "Must be a valid Nigeria phone number " +
                            "e.g. 08012345678 or +2348012345678"
            )
            String phoneNumber
    ){}

    public record LoginRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Must be a valid email address")
            String email,

            @NotBlank(message = "Password is required")
            String password,

            String deviceId
    ){}

    public record RefreshTokenRequest(
            @NotBlank(message = "Refresh token is required")
            String refreshToken
    ){}

    public record SetPinRequest(
            @NotBlank(message = "PIN is required")
            @Pattern(
                    regexp = "^\\d{4,6}$",
                    message = "PIN must be 4 to 6 digits"
            )
            String pin,

            @NotBlank(message = "Current password is required")
            String currentPassword
    ){}

    public record UpdateProfileRequest(

            @NotBlank(message = "Phone number is required")
            @Pattern(
                    regexp = "^(\\+234|0)[789][01]\\d{8}$",
                    message = "Must be a valid Nigerian phone number"
            )
            String phoneNumber

    ) {}
}
