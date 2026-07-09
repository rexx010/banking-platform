package com.bankplatform.auth.domain.port.out;

import java.util.Optional;

public interface RefreshTokenStore {
    /** Saves a refresh token linked to a userId with an expiry. */
    void save(String userId, String refreshToken, long ttlSeconds);

    /** Finds the userId that owns this refresh token. */
    Optional<String> findUserIdByToken(String refreshToken);

    /** Deletes one refresh token - used on logout from this device.*/
    void delete(String refreshToken);

    /** Deletes all refresh tokens for a user - logout from all devices.*/
    void deleteAllForUser(String userId);
}
