/**
 * Events Mobile Application
 *
 * File: UserDao.java
 *
 * Data Access Object defines
 * the SQL queries for authentication.
 *
 * Last Modified: 2026-04-04
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
    // Changed to String userID to match Firestore UID
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User getUserByIdSync(String userId);

    // Update login attempts and lockout status
    // Changed to String userID to match Firestore UID
    @Query("UPDATE users SET failedAttempts = :attempts, lockoutTimestamp = :lockout WHERE id = :userId")
    void updateLockoutStatus(String userId, int attempts, long lockout);

    // Update MFA details
    // Changed to String userID to match Firestore UID
    @Query("UPDATE users SET mfaCode = :code, mfaExpiry = :expiry WHERE id = :userId")
    void updateMfa(String userId, String code, long expiry);

    // For Password Recovery
    // Changed to String userID to match Firestore UID
    @Query("UPDATE users SET password = :newHashedPassword, failedAttempts = 0, lockoutTimestamp = 0 WHERE id = :userId")
    void resetPassword(String userId, String newHashedPassword);

    // Deletes an specific user by ID
    @Query("DELETE FROM users WHERE id = :userId")
    void deleteUserById(String userId);
}