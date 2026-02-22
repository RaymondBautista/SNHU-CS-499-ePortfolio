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

    public void authenticate(String identifier, String password, OnAuthListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            User user = userDao.findUserByIdentifier(identifier);
            if (user != null && user.password.equals(password)) {
                listener.onFinished(user);
            } else {
                listener.onFinished(null);
            }
        });
    }

    public void register(User user, OnRegisterListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userDao.insertUser(user);
            if (listener != null) listener.onFinished();
        });
    }
}