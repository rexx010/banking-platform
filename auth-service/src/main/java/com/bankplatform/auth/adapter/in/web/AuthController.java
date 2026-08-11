package com.bankplatform.auth.adapter.in.web;

import com.bankplatform.auth.adapter.in.web.dto.request.AuthRequests.*;
import com.bankplatform.auth.adapter.in.web.dto.response.AuthResponses.*;
import com.bankplatform.auth.adapter.in.web.mapper.AuthWebMapper;
import com.bankplatform.auth.application.usecase.AuthCommands;
import com.bankplatform.auth.application.usecase.AuthCommands.*;
import com.bankplatform.auth.domain.model.User;
import com.bankplatform.auth.domain.port.in.*;
import com.bankplatform.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final RegisterUserUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final SetTransactionPinUseCase setTransactionPinUseCase;
    private final AuthWebMapper mapper;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final VerifyPinUseCase verifyPinUseCase;

    /**
     POST /api/v1/auth/register
     Creates a new user account.
     Returns 201 Created on success.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request){
        User user = registerUseCase.register(mapper.toCommand(request));
        return ApiResponse.ok(
                mapper.toRegisterResponse(user),
                "Registration successful, Please verify your account."
        );
    }

    /**
     POST /api/v1/auth/login
     Authenticates the user and returns a token pair.
     */
    @PostMapping("/login")
    public ApiResponse<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request){
        TokenPair pair = loginUseCase.login(mapper.toCommand(request));
        return ApiResponse.ok(mapper.toTokenResponse(pair));
    }

    /**
     POST /api/v1/auth/refresh
     Exchanges a valid refresh token for a new token pair.
     */
    @PostMapping("/refresh")
    public ApiResponse<AuthTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request){
        TokenPair pair = refreshTokenUseCase.refresh(request.refreshToken());
        return ApiResponse.ok(mapper.toTokenResponse(pair));
    }

    /**
     POST /api/v1/auth/pin
     Sets or changes the transaction PIN.
     Requires the user to be logged in — @AuthenticationPrincipal
     injects the userId from the validated JWT.
     */
    @PostMapping("/pin")
    public ApiResponse<Void> setPin(@Valid @RequestBody SetPinRequest request,
                                    @AuthenticationPrincipal String userId){
        setTransactionPinUseCase.setPin(
                userId,
                request.pin(),
                request.currentPassword()
        );
        return ApiResponse.noContent("Transaction PIN set successfully");
    }

    /**
     GET /api/v1/auth/me
     Returns the current user's profile.
     Useful for the mobile app to check login state on startup.
     */
    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> me(
            @AuthenticationPrincipal String userId
    ) {
        User user = registerUseCase instanceof
                com.bankplatform.auth.application.usecase.AuthApplicationService svc
                ? svc.findById(userId)
                : null;
        // Temporary — we will refine this when adding GetUserUseCase
        return ApiResponse.ok(mapper.toProfileResponse(user));
    }

    /**
     * PATCH /api/v1/auth/profile
     * Updates the authenticated user's profile.
     *
     * Why PATCH and not PUT?
     * PUT replaces the entire resource — you must send every field.
     * PATCH applies a partial update — you only send what changed.
     * Since we only allow updating the phone number right now,
     * PATCH is semantically correct.
     */
    @PatchMapping("/profile")
    public ApiResponse<UserProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal String userId
    ) {
        User updated = updateUserProfileUseCase.updateProfile(
                userId,
                new AuthCommands.UpdateProfileCommand(request.phoneNumber())
        );
        return ApiResponse.ok(
                mapper.toProfileResponse(updated),
                "Profile updated successfully"
        );
    }

    /**
     * POST /internal/auth/pin/verify
     * Called by transfer-service before any money movement.
     * Verifies the transaction PIN without exposing the hash.
     * Not routed through API Gateway — internal network only.
     */
    @PostMapping("/internal/auth/pin/verify")
    public ApiResponse<PinVerificationResponse> verifyPin(
            @RequestBody PinVerificationRequest request
    ) {
        boolean valid = verifyPinUseCase.verifyPin(
                request.userId(),
                request.pin()
        );
        return ApiResponse.ok(new PinVerificationResponse(valid));
    }
}
