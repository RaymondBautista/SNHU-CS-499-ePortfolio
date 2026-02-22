/**
 * Events Mobile Application
 *
 * File: LoginViewModel.java
 *
 * Handles the logic of the Log In / Sign Up
 * screen. It checks for empty fields, validates
 * password length, and communicates with the
 * database room repository.
 *
 * Last Modified: 2026-02-22
 * Version: 2.0.0
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.snhu.events.model.User;
import com.snhu.events.repository.UserRepository;

public class LoginViewModel extends AndroidViewModel {

    // User table repository instance
    private final UserRepository repository;

    // LiveData: The "Pipeline" the Activity watches
    private final MutableLiveData<Boolean> isSignUpMode = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final MutableLiveData<User> authenticatedUser = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
    }

    // Getters for the UI to observe
    public LiveData<Boolean> getIsSignUpMode() { return isSignUpMode; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<User> getAuthenticatedUser() { return authenticatedUser; }

    // Toggle between Login and Sign Up views
    public void toggleMode() {
        Boolean current = isSignUpMode.getValue();
        isSignUpMode.setValue(current != null && !current);
        errorMessage.setValue(null);    // Clear errors when switching
    }

    // Decide whether to Log In or Register
    public void submitForm(String emailOrUser, String emailOnly, String password, String username, String phone) {
        // Validates password length
        if (password == null || password.length() < 8) {
            errorMessage.setValue("Password must be at least 8 characters");
            return;
        }

        // Sign Up Logic
        if (Boolean.TRUE.equals(isSignUpMode.getValue())) {
            if (emailOnly.isEmpty() || username.isEmpty() || phone.isEmpty()) {
                errorMessage.setValue("All fields are required");
                return;
            }

            User newUser = new User(emailOnly, username, password, phone);

            // Pass a callback to wait for the DB to finish saving
            repository.register(newUser, () -> {
                // Use postValue because we are coming back from a background thread
                isSignUpMode.postValue(false);
                errorMessage.postValue("Registration successful! Please log in.");
            });

        } else {
            // Log In logic
            if (emailOrUser.isEmpty()) {
                errorMessage.setValue("Enter email or username");
                return;
            }

            repository.authenticate(emailOrUser, password, user -> {
                if (user != null) {
                    authenticatedUser.postValue(user);
                } else {
                    errorMessage.postValue("Incorrect user or password");
                }
            });
        }
    }
}
