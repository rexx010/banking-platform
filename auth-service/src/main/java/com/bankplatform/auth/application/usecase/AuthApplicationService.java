package com.bankplatform.auth.application.usecase;

import com.bankplatform.auth.application.usecase.AuthCommands.*;
import com.bankplatform.auth.domain.model.User;
import com.bankplatform.auth.domain.port.in.*;
import com.bankplatform.auth.domain.port.out.RefreshTokenStore;
import com.bankplatform.auth.domain.port.out.UserRepository;
import com.bankplatform.shared.exceptions.BankException;
import com.bankplatform.shared.exceptions.ErrorCode;
import com.bankplatform.shared.logging.MaskingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthApplicationService
        implements
            RegisterUserUseCase,
            LoginUseCase,
            RefreshTokenUseCase,
            SetTransactionPinUseCase,
            UpdateUserProfileUseCase {
    private final UserRepository userRepository;
    private final RefreshTokenStore refreshTokenStore;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    /** Register */
    @Override
    public User register(RegisterUserCommand command){
       log.info("Registering new user email={}",
               MaskingUtil.maskEmail(command.email()));

       if (userRepository.existsByEmail(command.email())){
           throw new BankException(
                   ErrorCode.DUPLICATE_RESOURCE,
                   "An account with this email already exists"
           );
       }
       String passwordHash = passwordEncoder.encode(command.rawPassword());
       User user = User.register(command.email(), passwordHash, command.phoneNumber());
       User saved = userRepository.save(user);

       log.info("User registered userId={}", saved.getId());
       return saved;
    }

    /** Login */
    @Override
    public TokenPair login(LoginCommand command){
        log.info("Login attempt email={}",
                MaskingUtil.maskEmail(command.email()));

//        find user by email
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() ->
                        new BankException(ErrorCode.AUTH_INVALID_CREDENTIALS));

//        first checked if the account is locked before checking the password
        if (user.isLocked()) {
            log.warn("Login attemp on locked account userId={}",
                    user.getId());
            throw new BankException(ErrorCode.AUTH_ACCOUNT_LOCKED);
        }

//        BCrypt comparison, checking the password
        if(!passwordEncoder.matches(
                command.rawPassword(), user.getPasswordHash())){
            user.recordFailedLogin();
            userRepository.save(user);

            log.warn("Failed login attempt userId={} attempts={}",
                    user.getId(), user.getFailedLoginAttempts());
            throw new BankException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }
//        Password is correct, record success and issue tokens
        user.recordSuccessfulLogin();
        userRepository.save(user);
        // Put userId in MDC so all subsequent logs in this
        // request carry the user identity automatically
        MDC.put("userId", user.getId());

//        Generate token
        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getEmail(), user.getRoles());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        refreshTokenStore.save(
                user.getId(),
                refreshToken,
                jwtService.getAccessTtlSeconds()
        );
        log.info("Login successful userId={}", user.getId());

        return new TokenPair(
                accessToken,
                refreshToken,
                jwtService.getAccessTtlSeconds(),
                user.getId(),
                user.getEmail()
        );
    }

//    Refresh
    @Override
    public TokenPair refresh(String refreshToken){
//        first validate the jwt signature... no I/O
        String userId = jwtService.extractUserId(refreshToken)
                .orElseThrow(() ->
                        new BankException(ErrorCode.AUTH_REFRESH_INVALID));

//        verify token exists in redis... deleted on logout
        String storedUserId = refreshTokenStore
                .findUserIdByToken(refreshToken)
                .orElseThrow(() ->
                        new BankException(ErrorCode.AUTH_REFRESH_INVALID));
        if(!userId.equals(storedUserId)){
            throw new BankException(ErrorCode.AUTH_REFRESH_INVALID);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new BankException(ErrorCode.AUTH_REFRESH_INVALID));
        // Token rotation — delete old, issue new pair.
        // If a stolen refresh token is used, the legitimate
        // user's next refresh fails, alerting them.
        refreshTokenStore.delete(refreshToken);
        String newAccessToken  = jwtService.generateAccessToken(
                user.getId(), user.getEmail(), user.getRoles());
        String newRefreshToken = jwtService.generateRefreshToken(user.getId());

        refreshTokenStore.save(
                user.getId(),
                newRefreshToken,
                jwtService.getRefreshTtlSeconds()
        );

        log.info("Tokens refreshed userId={}", userId);

        return new TokenPair(
                newAccessToken,
                newRefreshToken,
                jwtService.getAccessTtlSeconds(),
                user.getId(),
                user.getEmail()
        );
    }

//    Set pin
    @Override
    public void setPin(
            String userId,
            String rawPin,
            String currentPassword
    ){
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new BankException(ErrorCode.RESOURCE_NOT_FOUND));
//        this requires the current password before pin can be changed.
//        it prevents someone with an unlocked phone from changing the PIN.
        if(!passwordEncoder.matches(
                currentPassword, user.getPasswordHash())){
            throw new BankException(
                    ErrorCode.AUTH_INVALID_CREDENTIALS,
                    "Current password is incorrect"
            );
        }
        String pinHash = passwordEncoder.encode(rawPin);
        user.setTransactionPin(pinHash);
        userRepository.save(user);

        log.info("Transaction PIN set userId={}", userId);
    }

    @Transactional(readOnly = true)
    public User findById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BankException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    // ── Update Profile ────────────────────────────────────

    @Override
    public User updateProfile(
            String userId,
            UpdateProfileCommand command
    ) {
        log.info("Updating profile for userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new BankException(ErrorCode.RESOURCE_NOT_FOUND,
                                "User not found"));

        user.updatePhoneNumber(command.phoneNumber());

        User saved = userRepository.save(user);

        log.info("Profile updated userId={}", userId);
        return saved;
    }
}
