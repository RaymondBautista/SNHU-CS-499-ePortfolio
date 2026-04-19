/**
 * Events Mobile Application
 *
 * File: UserRepository.java
 *
 * Abstract the database calls, acting as
 * a bridge between ViewModels and the database
 *
 * Last Modified: 2026-02-22
 * Version: 2.0.0
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.repository;

import com.snhu.events.data.AppDatabase;
import com.snhu.events.data.UserDao;
import com.snhu.events.model.User;

import org.mindrot.jbcrypt.BCrypt;

public class UserRepository {
    private final UserDao userDao;

    public UserRepository(android.app.Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
    }

    public interface OnAuthListener {
        void onFinished(User user);
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

    public void authenticate(String identifier, String password, OnAuthListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Fetch the user from DB by identifier (email or username)
            User user = userDao.findUserByIdentifier(identifier);

            // Check if user exists and the plain-text input matches the stored hash
            if (user != null && BCrypt.checkpw(password, user.password)) {
                // Success: return the user object for session management
                listener.onFinished(user);
            } else {
                // Failure: return null
                listener.onFinished(null);
            }
        });
    }
}