/**
 * Events Mobile Application
 *
 * File: UserRepository.java
 *
 * Abstract the database calls, acting as
 * a bridge between ViewModels and the database
 *
 * Last Modified: 2026-03-21
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.repository;

import android.app.Application;
import android.content.Context;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.snhu.events.data.AppDatabase;
import com.snhu.events.data.UserDao;
import com.snhu.events.model.User;
import com.snhu.events.service.MfaSmsWorker;

import org.mindrot.jbcrypt.BCrypt;

public class UserRepository {
    private final UserDao userDao;
    private final Context context;

    // Create database instance and retrieve application context
    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
        this.context = application.getApplicationContext();
    }

    // Public listener interfaces
    public interface OnAuthListener {
        void onFinished(User user, String statusMessage);
    }

    public interface OnRegisterListener {
        void onFinished();
    }

    /* Hashes user's password using BCrypt algorithm
     * applying salting for safe credential handling.
     * Then execute the service to register the user.
     */
    public void register(User user, OnRegisterListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Hash the password with a generated salt before saving
            String hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt());
            user.password = hashedPassword; // Overwrite plain text with hash

            userDao.insertUser(user);

            if (listener != null) {
                listener.onFinished();
            }
        });
    }

    /*
     * Secure authentication logic that compares the hashed user input
     * against the stored hash for credential verification.
     *
     * Enhancements:
     * - Detect and mitigate brute-force attacks by tracking failed login attempts
     *   and applying a cooldown period or temporary account lockout after a threshold.
     * - Generate a 6-digit MFA code with a 5-minute expiration to verify user identity via SMS.
     * - Monitor and log failed authentication attempts for security auditing.
     */
    public void authenticate(String identifier, String password, OnAuthListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            User user = userDao.findUserByIdentifier(identifier);
            long now = System.currentTimeMillis();

            if (user == null) {
                listener.onFinished(null, "User not found");
                return;
            }

            // Check Brute-Force Attacks and implements a cooldown
            if (user.lockoutTimestamp > now) {
                long remaining = (user.lockoutTimestamp - now) / 60000;
                listener.onFinished(null, "Account locked. Try again in " + (remaining + 1) + " mins.");
                return;
            }

            // Check credentials against stored hash using BCrypt
            if (BCrypt.checkpw(password, user.password)) {
                // Success - Reset attempts
                userDao.updateLockoutStatus(user.id, 0, 0);

                // Generate 6-digit random MFA Code
                String mfaCode = String.valueOf((int)(Math.random() * 900000) + 100000);
                userDao.updateMfa(user.id, mfaCode, now + (5 * 60000)); // 5 min expiry

                // Trigger the SMS Worker
                triggerMfaWorker(user.phone, mfaCode);

                listener.onFinished(user, "MFA_SENT");
            } else {
                // Failure - Increment attempts
                int attempts = user.failedAttempts + 1;
                long lockout = (attempts >= 5) ? now + (15 * 60000) : 0;

                userDao.updateLockoutStatus(user.id, attempts, lockout);

                String msg = (attempts >= 5) ? "Too many attempts. Account locked for 15m."
                        : "Incorrect password (" + attempts + "/5)";
                listener.onFinished(null, msg);
            }
        });
    }

    // Helper method to trigger MFA SMS worker
    private void triggerMfaWorker(String phone, String code) {
        Data data = new Data.Builder()
                .putString("PHONE", phone)
                .putString("MFA_CODE", code)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(MfaSmsWorker.class)
                .setInputData(data)
                .build();

        WorkManager.getInstance(context).enqueue(request);
    }
}