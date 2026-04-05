/**
 * Events Mobile Application
 *
 * File: User.java
 *
 * Defines the structure of the users table.
 *
 * Last Modified: 2026-04-04
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.model;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = {@Index(value = {"email"}, unique = true), @Index(value = {"username"}, unique = true)})
public class User {

    // Change from auto-incremental int to String to store Firebase UID
    @PrimaryKey
    @NonNull
    public String id;

    // Users table's fields
    public String email;
    public String username;
    public String password;
    public String phone;

    // Security Variables
    public int failedAttempts;       // Tracks brute-force attempts
    public long lockoutTimestamp;    // Time when lockout expires (ms)
    public String mfaCode;           // Temporary 6-digit SMS code
    public long mfaExpiry;           // Time when MFA code expires

    // Empty constructor for Firestore mapping
    public User() {}

    public User(@NonNull String id, String username, String email, String phone) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.failedAttempts = 0;
        this.lockoutTimestamp = 0;
        this.mfaExpiry = 0;
    }
}
