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

public class UserRepository {
    private final UserDao userDao;
    private final ExecutorService executorService;

    // Get database and DAO, then execute the service in a new thread
    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    // Abstract method to register an user through DAO
    public void register(User user) {
        executorService.execute(() -> userDao.registerUser(user));
    }

    // Compare user input and authenticate for log in
    public User login(String identifier, String password) {
        // Database operations must happen off the main thread
        return userDao.login(identifier, password);
    }
}