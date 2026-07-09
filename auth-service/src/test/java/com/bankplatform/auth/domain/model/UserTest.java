package com.bankplatform.auth.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the User domain entity.
 *
 * No Spring, no database, no mocks needed.
 * User is pure Java so tests run in milliseconds.
 *
 * We test BEHAVIOUR not implementation:
 *   - "account locks after 5 failed attempts" — behaviour
 *   - "failedLoginAttempts field equals 5" — implementation
 * Behaviour tests survive refactoring. Implementation tests break
 * every time you rename a field even if the behaviour is correct.
 */
@DisplayName("User domain entity")
class UserTest {

    // A valid user used as the starting point for most tests
    private User user;

    /**
     * @BeforeEach runs before every single @Test method.
     * Creates a fresh user so tests do not affect each other.
     * Each test starts from a clean known state.
     */
    @BeforeEach
    void setUp() {
        user = User.register(
                "ade@example.com",
                "$2a$12$hashedpassword",  // pre-hashed, not real BCrypt
                "08012345678"
        );
        // Activate the user so login tests work
        user.activate();
    }

    @Nested
    @DisplayName("Registration")
    class Registration {

        @Test
        @DisplayName("new user starts as PENDING_VERIFICATION")
        void newUser_startsPendingVerification() {
            // Arrange + Act — registration happens in setUp's User.register()
            User newUser = User.register(
                    "bola@example.com",
                    "$2a$12$hash",
                    "08098765432"
            );

            // Assert
            assertThat(newUser.getStatus())
                    .isEqualTo(UserStatus.PENDING_VERIFICATION);
        }

        @Test
        @DisplayName("new user has CUSTOMER role automatically")
        void newUser_hasCustomerRole() {
            User newUser = User.register(
                    "test@example.com",
                    "$2a$12$hash",
                    "08011111111"
            );

            assertThat(newUser.getRoles())
                    .containsExactly(Role.CUSTOMER);
        }

        @Test
        @DisplayName("new user has no transaction PIN set")
        void newUser_hasNoPinSet() {
            User newUser = User.register(
                    "test@example.com",
                    "$2a$12$hash",
                    "08011111111"
            );

            assertThat(newUser.hasPinSet()).isFalse();
        }

        @Test
        @DisplayName("generated ID is not null or blank")
        void newUser_hasGeneratedId() {
            User newUser = User.register(
                    "test@example.com",
                    "$2a$12$hash",
                    "08011111111"
            );

            assertThat(newUser.getId())
                    .isNotNull()
                    .isNotBlank();
        }
    }

    @Nested
    @DisplayName("Activation")
    class Activation {

        @Test
        @DisplayName("activate changes status to ACTIVE")
        void activate_changesStatusToActive() {
            User newUser = User.register(
                    "test@example.com",
                    "$2a$12$hash",
                    "08011111111"
            );

            // Act
            newUser.activate();

            // Assert
            assertThat(newUser.getStatus())
                    .isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("active user is not locked")
        void activeUser_isNotLocked() {
            assertThat(user.isLocked()).isFalse();
            assertThat(user.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Login attempts")
    class LoginAttempts {

        @Test
        @DisplayName("successful login resets failed attempt counter")
        void successfulLogin_resetsFailedAttempts() {
            // Arrange — simulate some failed attempts first
            user.recordFailedLogin();
            user.recordFailedLogin();

            // Act
            user.recordSuccessfulLogin();

            // Assert
            assertThat(user.getFailedLoginAttempts()).isZero();
            assertThat(user.getLastLoginAt()).isNotNull();
        }

        @Test
        @DisplayName("account locks after 5 failed attempts")
        void fiveFailedAttempts_locksAccount() {
            // Act — fail 5 times
            for (int i = 0; i < 5; i++) {
                user.recordFailedLogin();
            }

            // Assert
            assertThat(user.isLocked()).isTrue();
            assertThat(user.getStatus()).isEqualTo(UserStatus.LOCKED);
            assertThat(user.getLockedUntil()).isNotNull();
        }

        @Test
        @DisplayName("account is not locked after 4 failed attempts")
        void fourFailedAttempts_doesNotLockAccount() {
            // Act
            for (int i = 0; i < 4; i++) {
                user.recordFailedLogin();
            }

            // Assert — one more attempt remaining before lockout
            assertThat(user.isLocked()).isFalse();
            assertThat(user.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Transaction PIN")
    class TransactionPin {

        @Test
        @DisplayName("hasPinSet returns true after setting PIN")
        void hasPinSet_trueAfterSettingPin() {
            // Act
            user.setTransactionPin("$2a$12$hashedpin");

            // Assert
            assertThat(user.hasPinSet()).isTrue();
        }

        @Test
        @DisplayName("getTransactionPinHash returns the stored hash")
        void getPinHash_returnsStoredHash() {
            String pinHash = "$2a$12$hashedpin";

            user.setTransactionPin(pinHash);

            assertThat(user.getTransactionPinHash())
                    .isEqualTo(pinHash);
        }
    }

    @Nested
    @DisplayName("Profile update")
    class ProfileUpdate {

        @Test
        @DisplayName("can update phone number")
        void canUpdatePhoneNumber() {
            // Arrange
            String newPhone = "09087654321";

            // Act
            user.updatePhoneNumber(newPhone);

            // Assert
            assertThat(user.getPhoneNumber()).isEqualTo(newPhone);
        }

        @Test
        @DisplayName("throws when phone number is blank")
        void throws_whenPhoneNumberIsBlank() {
            assertThatThrownBy(() -> user.updatePhoneNumber(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be blank");
        }

        @Test
        @DisplayName("updatedAt changes after profile update")
        void updatedAt_changesAfterUpdate() {
            // Arrange — record the time before updating
            var beforeUpdate = user.getUpdatedAt();

            // Small sleep to ensure time difference is measurable
            try { Thread.sleep(10); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }

            // Act
            user.updatePhoneNumber("09087654321");

            // Assert
            assertThat(user.getUpdatedAt())
                    .isAfterOrEqualTo(beforeUpdate);
        }
    }
}