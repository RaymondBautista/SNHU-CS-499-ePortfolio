/**
 * Events Mobile Application
 *
 * File: UserDao.java
 *
 * Data Access Object defines
 * the SQL queries for authentication.
 *
 * Last Modified: 2026-02-08
 * Version: 1.0.0
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.data;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.snhu.events.model.User;

@Dao
public interface UserDao {
    @Insert
    void registerUser(User user);

    // Finds user by email or username
    @Query("SELECT * FROM users WHERE email = :identifier OR username = :identifier LIMIT 1")
    User findUserByIdentifier(String identifier);

    // SMS Worker: Synchronous fetch to get the user's phone number
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User getUserByIdSync(int userId);
}
