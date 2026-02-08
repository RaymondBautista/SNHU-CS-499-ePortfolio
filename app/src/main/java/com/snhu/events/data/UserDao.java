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

    // Supports login via either Email OR Username
    @Query("SELECT * FROM users WHERE (email = :identifier OR username = :identifier) AND password = :password LIMIT 1")
    User login(String identifier, String password);

    // Check if the user exists looking by email or username
    @Query("SELECT * FROM users WHERE email = :email OR username = :username LIMIT 1")
    User checkUserExists(String email, String username);
}
