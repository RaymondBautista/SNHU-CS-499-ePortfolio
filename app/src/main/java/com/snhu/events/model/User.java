/**
 * Events Mobile Application
 *
 * File: User.java
 *
 * Defines the structure of the users table.
 *
 * Last Modified: 2026-03-22
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.model;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = {@Index(value = {"email"}, unique = true), @Index(value = {"username"}, unique = true)})
public class User {

    // Set id as auto-incremental primary key
    @PrimaryKey(autoGenerate = true)
    public int id;

    // Users table's fields
    public String email;
    public String username;
    public String password;
    public String phone;

    // Security Variables
    public int failedAttempts = 0;       // Tracks brute-force attempts
    public long lockoutTimestamp = 0;    // Time when lockout expires (ms)
    public String mfaCode;               // Temporary 6-digit SMS code
    public long mfaExpiry = 0;           // Time when MFA code expires

    public User(String email, String username, String password, String phone) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.phone = phone;
    }
}
