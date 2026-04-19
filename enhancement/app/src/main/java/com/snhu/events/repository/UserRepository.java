/**
 * Events Mobile Application
 *
 * File: UserRepository.java
 *
 * Abstract the database calls, acting as
 * a bridge between ViewModels and the database
 *
 * Last Modified: 2026-04-04
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.repository;

import android.app.Application;
import android.content.Context;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.snhu.events.data.AppDatabase;
import com.snhu.events.data.UserDao;
import com.snhu.events.model.User;
import com.snhu.events.service.MfaSmsWorker;

import org.mindrot.jbcrypt.BCrypt;

public class UserRepository {
    private final UserDao userDao;
    private final Context context;

    // Firestore Instance
    private final FirebaseFirestore firestore;

    // Create database instance and retrieve application context
    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
        this.context = application.getApplicationContext();
        this.firestore = FirebaseFirestore.getInstance();   // Initialize Firestore
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

            // Generate a unique String ID from Firestore for the new user
            String newUserId = firestore.collection("users").document().getId();
            user.id = newUserId;

            // Hash the password with a generated salt before saving
            String hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt());
            user.password = hashedPassword; // Overwrite plain text with hash

            // Saves locally for the current device
            userDao.insertUser(user);

            // Saves to Cloud Master Source of Truth
            firestore.collection("users").document(user.id).set(user);

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
     * - Cloud First Approach: Safely authenticates on any device without downloading all users.
     */
    public void authenticate(String identifier, String password, OnAuthListener listener) {
        // Query the Cloud first to find the specific user by email or username
        firestore.collection("users")
                .where(Filter.or(
                        Filter.equalTo("email", identifier),
                        Filter.equalTo("username", identifier)
                ))
                .limit(1) // Ensure we only get 1 result
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Extract ONLY this user's data from the cloud
                        User cloudUser = task.getResult().getDocuments().get(0).toObject(User.class);

                        // Perform security checks safely on a background thread
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            long now = System.currentTimeMillis();

                            // if the penalty time has expired, reset the brute-force counters
                            if (cloudUser.lockoutTimestamp > 0 && now > cloudUser.lockoutTimestamp) {
                                cloudUser.failedAttempts = 0;
                                cloudUser.lockoutTimestamp = 0;
                            }

                            // Check Brute-Force Attacks and implements a cooldown
                            if (cloudUser.lockoutTimestamp > now) {
                                long remaining = (cloudUser.lockoutTimestamp - now) / 60000;
                                listener.onFinished(null, "Account locked. Try again in " + (remaining + 1) + " mins.");
                                return;
                            }

                            // Check credentials against stored hash using BCrypt
                            if (BCrypt.checkpw(password, cloudUser.password)) {
                                // Success - Reset attempts on Cloud
                                cloudUser.failedAttempts = 0;
                                cloudUser.lockoutTimestamp = 0;

                                // Generate 6-digit random MFA Code and set 5-min expiry
                                String mfaCode = String.valueOf((int)(Math.random() * 900000) + 100000);
                                long expiry = now + (5 * 60000);
                                cloudUser.mfaCode = mfaCode;
                                cloudUser.mfaExpiry = expiry;

                                // Once authenticated, cache this user locally
                                // and update their new MFA/Lockout status in the cloud
                                userDao.insertUser(cloudUser);
                                firestore.collection("users").document(cloudUser.id).set(cloudUser);

                                // Trigger the SMS Worker
                                triggerMfaWorker(cloudUser.phone, mfaCode);
                                listener.onFinished(cloudUser, "MFA_SENT");
                            } else {
                                // Failure - Increment attempts locally and on Cloud
                                int attempts = cloudUser.failedAttempts + 1;
                                long lockout = (attempts >= 5) ? now + (15 * 60000) : 0;

                                cloudUser.failedAttempts = attempts;
                                cloudUser.lockoutTimestamp = lockout;

                                // Update Cloud so they are locked out on ALL devices, and cache locally
                                userDao.insertUser(cloudUser);
                                firestore.collection("users").document(cloudUser.id).set(cloudUser);

                                String msg = (attempts >= 5) ? "Too many attempts. Account locked for 15m."
                                        : "Incorrect password (" + attempts + "/5)";
                                listener.onFinished(null, msg);
                            }
                        });
                    } else {
                        // User doesn't exist in the Cloud
                        listener.onFinished(null, "User not found or network error.");
                    }
                });
    }

    // Verify MFA code. Kept this operation local as its scope only covers the users' current device
    public void verifyMfa(String userId, String enteredCode, OnAuthListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            User user = userDao.getUserByIdSync(userId);
            long now = System.currentTimeMillis();

            // Check if the entered code is equal to the saved on the database and time has not expired
            if (user != null && enteredCode.equals(user.mfaCode) && now < user.mfaExpiry) {
                listener.onFinished(user, "MFA_SUCCESS");
            } else {
                listener.onFinished(null, "Incorrect or expired code");
            }
        });
    }

    // Find the user and send MFA SMS for password recovery without password check
    // Updated with Cloud First Approach: Safely authenticates on any device without downloading all users
    public void initiatePasswordRecovery(String identifier, OnAuthListener listener) {
        firestore.collection("users")
                .where(Filter.or(
                        Filter.equalTo("email", identifier),
                        Filter.equalTo("username", identifier)
                ))
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        User cloudUser = task.getResult().getDocuments().get(0).toObject(User.class);

                        // Generate new MFA code
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            String code = String.valueOf((int)(Math.random() * 900000) + 100000);
                            long expiry = System.currentTimeMillis() + (5 * 60000);

                            cloudUser.mfaCode = code;
                            cloudUser.mfaExpiry = expiry;

                            // Cache locally and update cloud
                            userDao.insertUser(cloudUser);
                            firestore.collection("users").document(cloudUser.id).set(cloudUser);

                            triggerMfaWorker(cloudUser.phone, code);
                            listener.onFinished(cloudUser, "RECOVERY_MFA_SENT");
                        });
                    } else {
                        listener.onFinished(null, "User not found.");
                    }
                });
    }

    // Reset user password
    public void resetPassword(String userId, String newPassword, OnRegisterListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            userDao.resetPassword(userId, hashedPassword);  // Resets locally
            // Resets on cloud
            firestore.collection("users").document(userId).update(
                    "password", hashedPassword, "failedAttempts", 0, "lockoutTimestamp", 0);
            if (listener != null) listener.onFinished();
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