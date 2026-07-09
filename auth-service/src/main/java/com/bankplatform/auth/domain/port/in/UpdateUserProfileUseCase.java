package com.bankplatform.auth.domain.port.in;

import com.bankplatform.auth.application.usecase.AuthCommands.UpdateProfileCommand;
import com.bankplatform.auth.domain.model.User;

/**
 * IN-PORT: updates a user's profile information.
 *
 * Separated from registration because they have different
 * rules. Registration creates a new user with a password.
 * Profile update changes existing non-sensitive fields
 * and requires the user to already be authenticated.
 */
public interface UpdateUserProfileUseCase {
    User updateProfile(String userId, UpdateProfileCommand command);
}