/**
 * Events Mobile Application
 *
 * File: UserDao.java
 *
 * Data Access Object defines
 * the SQL queries for authentication.
 *
 * Last Modified: 2026-03-22
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.snhu.events.model.User;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

    // Retrieve user information by id or username
    @Query("SELECT * FROM users WHERE email = :identifier OR username = :identifier LIMIT 1")
    User findUserByIdentifier(String identifier);

    // Get user by ID synced for the SMS worker
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User getUserByIdSync(int userId);

    // Update login attempts and lockout status
    @Query("UPDATE users SET failedAttempts = :attempts, lockoutTimestamp = :lockout WHERE id = :userId")
    void updateLockoutStatus(int userId, int attempts, long lockout);

    // Update MFA details
    @Query("UPDATE users SET mfaCode = :code, mfaExpiry = :expiry WHERE id = :userId")
    void updateMfa(int userId, String code, long expiry);

    // For Password Recovery
    @Query("UPDATE users SET password = :newHashedPassword, failedAttempts = 0, lockoutTimestamp = 0 WHERE id = :userId")
    void resetPassword(int userId, String newHashedPassword);
}