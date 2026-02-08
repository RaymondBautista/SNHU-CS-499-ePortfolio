/**
 * Events Mobile Application
 *
 * File: UserRepository.java
 *
 * Abstract the database calls, acting as
 * a bridge between ViewModels and the database
 *
 * Last Modified: 2026-02-08
 * Version: 1.0.0
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.repository;
import android.app.Application;
import com.snhu.events.data.AppDatabase;
import com.snhu.events.data.UserDao;
import com.snhu.events.model.User;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.mindrot.jbcrypt.BCrypt;

public class UserRepository {
    private final UserDao userDao;
    private final ExecutorService executorService;

    // Get database and DAO, then execute the service in a new thread
    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    /* Hashes user's password using BCrypt algorithm
     * applying salting for safe credential handling.
     * Then execute the service to register the user.
     */
    public void register(User user) {
        executorService.execute(() -> {
            // Hash the password with a generated salt
            String hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt());
            user.password = hashedPassword; // Overwrite plain text with hash
            userDao.registerUser(user);
        });
    }

    // Define a simple callback interface
    public interface LoginCallback {
        void onResult(boolean success);
    }

    public void authenticate(String identifier, String password, LoginCallback callback) {
        executorService.execute(() -> {
            // Fetch the user from DB by identifier
            User user = userDao.findUserByIdentifier(identifier);
            boolean isValid = false;

            if (user != null) {
                // Check if the plain-text input matches the stored hash
                isValid = BCrypt.checkpw(password, user.password);
            }

            // Return the result to the caller
            callback.onResult(isValid);
        });
    }
}