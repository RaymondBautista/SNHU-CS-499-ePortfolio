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
 * Last Modified: 2026-03-21
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

    // List of the authentication steps for the State Machine
    public enum AuthState { LOGIN, SIGNUP, MFA_LOGIN, MFA_RECOVERY, RESET_PASSWORD }

    // LiveData: The "Pipeline" the Activity watches
    private final MutableLiveData<AuthState> authState = new MutableLiveData<>(AuthState.LOGIN);
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();
    private final MutableLiveData<String> mfaErrorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> maskedPhone = new MutableLiveData<>();
    private final MutableLiveData<User> authenticatedUser = new MutableLiveData<>();

    private User pendingUser;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        this.repository = new UserRepository(application);
    }

    // Getters for the UI to observe
    public LiveData<AuthState> getAuthState() { return authState; }
    public LiveData<String> getStatusMessage() { return statusMessage; }
    public LiveData<String> getMfaErrorMessage() { return mfaErrorMessage; }
    public LiveData<String> getMaskedPhone() { return maskedPhone; }
    public LiveData<User> getAuthenticatedUser() { return authenticatedUser; }

    // Toggle between Log In and Sign Up
    public void toggleSignUpMode() {
        if (authState.getValue() == AuthState.LOGIN) {
            authState.postValue(AuthState.SIGNUP);
        } else {
            authState.postValue(AuthState.LOGIN);
        }
    }

    // Register new user in the database
    public void register(String email, String username, String password, String phone) {
        // Check for missing fields on the register form
        if (email.isEmpty() || username.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            statusMessage.postValue("Please fill all fields");
            return;
        }
        // Check for password length
        if (password.length() < 8) {
            statusMessage.postValue("Password must be at least 8 characters");
            return;
        }
        // Create new user record instance
        User newUser = new User(email, username, password, phone);
        // Pass a callback to wait for the DB to finish saving
        repository.register(newUser, () -> {
            statusMessage.postValue("Registration successful! Please log in.");
            authState.postValue(AuthState.LOGIN);
        });
    }

    // Compare user credentials against database to login
    public void login(String identifier, String password) {
        repository.authenticate(identifier, password, (user, message) -> {
            if ("MFA_SENT".equals(message) && user != null) {
                pendingUser = user;
                setMaskedPhone(user.phone);
                authState.postValue(AuthState.MFA_LOGIN);
            } else {
                statusMessage.postValue(message);
            }
        });
    }

    // Initiate password recovery workflow based on user's identifier
    public void startPasswordRecovery(String identifier) {
        // Check if the user specified identity
        if (identifier == null || identifier.isEmpty()) {
            statusMessage.postValue("Please enter your Username or Email first.");
            return;
        }
        repository.initiatePasswordRecovery(identifier, (user, message) -> {
            if (user != null) { // Existing user
                pendingUser = user; // Create instance
                setMaskedPhone(user.phone); // Get phone for MFA hint
                authState.postValue(AuthState.MFA_RECOVERY);    // Change state
                statusMessage.postValue("Recovery code sent!");
            } else {
                statusMessage.postValue(message);
            }
        });
    }

    // Verify MFA code user input against temporary code saved on the DB
    public void verifyMfa(String code) {
        if (pendingUser == null) return;
        mfaErrorMessage.postValue(""); // Clear previous errors

        // If the MFA is used for recovery, set the state machine to RESET_PASSWORD
        repository.verifyMfa(pendingUser.id, code, (user, message) -> {
            if ("MFA_SUCCESS".equals(message)) {
                if (authState.getValue() == AuthState.MFA_RECOVERY) {
                    authState.postValue(AuthState.RESET_PASSWORD);
                } else {
                    authenticatedUser.postValue(pendingUser);
                }
            } else {
                mfaErrorMessage.postValue("Incorrect code, try again.");
            }
        });
    }

    // Saves a new password by updating the existing one on the database
    public void saveNewPassword(String newPassword) {
        // Check for password length
        if (newPassword == null || newPassword.length() < 8) {
            statusMessage.postValue("Password must be at least 8 characters.");
            return;
        }
        // if the password is valid, update it and set the State Machine to Log In
        repository.resetPassword(pendingUser.id, newPassword, () -> {
            statusMessage.postValue("Password updated! Please log in.");
            authState.postValue(AuthState.LOGIN);
            pendingUser = null;
        });
    }

    // Helper method to extract the latest 4 digits of the user phone number for MFA
    private void setMaskedPhone(String phone) {
        if (phone != null && phone.length() >= 4) {
            String lastFour = phone.substring(phone.length() - 4);
            // Show phone number hint to the user on screen for MFA
            maskedPhone.postValue("Introduce the security code sent to your number ending in " + lastFour);
        }
    }

}
