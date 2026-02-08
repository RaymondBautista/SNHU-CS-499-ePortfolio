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
 * Last Modified: 2026-02-08
 * Version: 1.0.0
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.viewmodel;
import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;

import com.snhu.events.model.User;
import com.snhu.events.repository.UserRepository;

public class LoginViewModel extends AndroidViewModel {

    // User table repository instance
    private final UserRepository repository;

    // LiveData: The "Pipeline" the Activity watches
    private final MutableLiveData<Boolean> isSignUpMode = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navigateToHome = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
    }

    // Getters for the UI to observe
    public LiveData<Boolean> getIsSignUpMode() { return isSignUpMode; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getNavigateToHome() { return navigateToHome; }

    // Toggle between Login and Sign Up views
    public void toggleMode() {
        Boolean currentMode = isSignUpMode.getValue();
        isSignUpMode.setValue(currentMode != null && !currentMode);
        errorMessage.setValue(null); // Clear errors when switching
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
            // Validate all fields are completed during registration
            if (emailOnly.isEmpty() || username.isEmpty() || phone.isEmpty()) {
                errorMessage.setValue("All fields are required for registration");
                return;
            }

            // Creates a new user and register it on the database
            User newUser = new User(emailOnly, username, password, phone);
            repository.register(newUser);

            // Switch to log in after successfully registering the user
            toggleMode();
            errorMessage.setValue("Registration successful! Please log in.");

        } else {
            // Log In logic
            // Check for empty username or email
            if (emailOrUser.isEmpty()) {
                errorMessage.setValue("Please enter your email or username");
                return;
            }

            // Call Repository (Async)
            repository.authenticate(emailOrUser, password, success -> {
                if (success) {
                    navigateToHome.postValue(true); // Trigger navigation
                } else {
                    errorMessage.postValue("Incorrect user or password");
                }
            });
        }
    }
}
