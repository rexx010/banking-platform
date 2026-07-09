package com.bankplatform.auth.domain.model;

import com.bankplatform.shared.util.IdGenerator;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

public class User {
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_DURATION_SECONDS = 30 * 60L;

    private final String id;
    private final String email;
    private final String passwordHash;
    private String phoneNumber;
    private String transactionPinHash;
    private final Set<Role> roles;
    private UserStatus status;
    private int failedLoginAttempts;
    private Instant lockedUntil;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;

    private User(
            String id,
            String email,
            String passwordHash,
            String phoneNumber,
            Instant createdAt
    ){
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phoneNumber = phoneNumber;
        this.roles = EnumSet.noneOf(Role.class);
        this.status = UserStatus.PENDING_VERIFICATION;
        this.failedLoginAttempts = 0;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    /**
     The only way to create a new User.
     Password must already be hashed before calling this —
     the domain entity never holds a raw password.
     */
    public static User register(
            String email,
            String passwordHash,
            String phoneNumber
    ){
        User user = new User(
                IdGenerator.generate(),
                email,
                passwordHash,
                phoneNumber,
                Instant.now()
        );
        user.roles.add(Role.CUSTOMER);
        user.status = UserStatus.PENDING_VERIFICATION;
        return user;
    }

    /**
     Called after a correct password is entered.
     Resets failed attempts and records the login time.
     */
    public void recordSuccessfulLogin(){
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.lastLoginAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     Called after a wrong password is entered.
     Locks the account after MAX_FAILED_ATTEMPTS.
     */
    public void recordFailedLogin(){
        this.failedLoginAttempts++;
        this.updatedAt = Instant.now();
        if (this.failedLoginAttempts >= MAX_FAILED_ATTEMPTS) {
            this.status = UserStatus.LOCKED;
            this.lockedUntil = Instant.now().plusSeconds(LOCK_DURATION_SECONDS);
        }
    }

    /**
     Marks the account as active after email/phone verification.
     */
    public void activate(){
        this.status = UserStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    /**
     Sets or changes the transaction PIN.
     The pin must already be hashed before calling this.
     */
    public void setTransactionPin(String pinHash) {
        this.transactionPinHash = pinHash;
        this.updatedAt          = Instant.now();
    }

    /**
     * Updates the user's phone number.
     * More fields can be added here as the product grows.
     * Keeping mutation methods explicit prevents accidental
     * changes — you cannot just set any field freely.
     */
    public void updatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Phone number cannot be blank"
            );
        }
        this.phoneNumber = phoneNumber;
        this.updatedAt   = Instant.now();
    }

    public boolean isActive(){
        return status == UserStatus.ACTIVE;
    }

    /**
     Checks if account is locked.
     Auto-unlocks if the lock duration has passed —
     no scheduled job needed.
     */
    public boolean isLocked() {
        if (status != UserStatus.LOCKED) return false;
        if (lockedUntil != null && Instant.now().isAfter(lockedUntil)) {
            this.status              = UserStatus.ACTIVE;
            this.failedLoginAttempts = 0;
            this.lockedUntil         = null;
            return false;
        }
        return true;
    }

    public boolean hasPinSet() {
        return transactionPinHash != null
                && !transactionPinHash.isBlank();
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getTransactionPinHash() {
        return transactionPinHash;
    }

    public Set<Role> getRoles() {
        return Set.copyOf(roles);
    }

    public UserStatus getStatus() {
        return status;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }
}
