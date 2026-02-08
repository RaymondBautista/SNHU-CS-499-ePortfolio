/**
 * Events Mobile Application
 *
 * File: LoginActivity.java
 *
 * Connects the activity login layout
 * to the ViewModel to manage view
 * elements effectively
 *
 * Last Modified: 2026-02-08
 * Version: 1.0.0
 *
 * Author: Raymond Bautista
 */
package com.snhu.events;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.snhu.events.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {

    // Associated ViewModel instance
    private LoginViewModel viewModel;

    // UI Elements
    private TextInputLayout layoutEmailOrUser, layoutEmailOnly, layoutUsernameOnly, layoutPhone, layoutPassword;
    private MaterialButton btnLogin, btnRegister, btnSignUpToggle;
    private TextView txtError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Bind Views (matching XML IDs)
        layoutEmailOrUser = findViewById(R.id.layoutEmailOrUser);
        layoutEmailOnly = findViewById(R.id.layoutEmailOnly);
        layoutUsernameOnly = findViewById(R.id.layoutUsernameOnly);
        layoutPhone = findViewById(R.id.layoutPhone);
        layoutPassword = findViewById(R.id.layoutPassword); // Shared field
        txtError = findViewById(R.id.txtError);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnSignUpToggle = findViewById(R.id.btnSignUpToggle);

        // Observe the current ViewModel State

        // Toggle Visibility based on Mode
        viewModel.getIsSignUpMode().observe(this, isSignUp -> {
            if (isSignUp) {
                /*
                 * Show the Sign Up fields,
                 * and hide the Log In ones,
                 * if the Sign Up mode is active.
                 * Log In is active by default.
                 */
                layoutEmailOrUser.setVisibility(View.GONE);
                layoutEmailOnly.setVisibility(View.VISIBLE);
                layoutUsernameOnly.setVisibility(View.VISIBLE);
                layoutPhone.setVisibility(View.VISIBLE);

                btnLogin.setVisibility(View.GONE);
                btnRegister.setVisibility(View.VISIBLE);
                btnSignUpToggle.setText(R.string.login_title); // "Log In"
            } else {
                /*
                 * Show the Log In fields
                 * and hide the Sign Up ones,
                 * if Sign Up is not active.
                 */
                layoutEmailOrUser.setVisibility(View.VISIBLE);
                layoutEmailOnly.setVisibility(View.GONE);
                layoutUsernameOnly.setVisibility(View.GONE);
                layoutPhone.setVisibility(View.GONE);

                btnLogin.setVisibility(View.VISIBLE);
                btnRegister.setVisibility(View.GONE);
                btnSignUpToggle.setText(R.string.signup_title); // "Sign Up"
            }
        });

        // Show Errors
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                txtError.setText(error);
                txtError.setVisibility(View.VISIBLE);
            } else {
                // Only show error messages if available
                txtError.setVisibility(View.GONE);
            }
        });

        // Navigate on Success
        viewModel.getNavigateToHome().observe(this, success -> {
            if (success) {
                // Navigate to the Event List Activity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close login screen so back button doesn't return here
            }
        });

        // Set Click Listeners
        btnSignUpToggle.setOnClickListener(v -> viewModel.toggleMode());

        // Both buttons call the same submit logic; the ViewModel decides what to do
        View.OnClickListener submitListener = v -> {
            String emailOrUser = getText(layoutEmailOrUser);
            String emailOnly = getText(layoutEmailOnly);
            String pass = getText(layoutPassword);
            String user = getText(layoutUsernameOnly);
            String phone = getText(layoutPhone);

            viewModel.submitForm(emailOrUser, emailOnly, pass, user, phone);
        };

        btnLogin.setOnClickListener(submitListener);
        btnRegister.setOnClickListener(submitListener);
    }

    // Helper to get text from TextInputLayout safely
    private String getText(TextInputLayout layout) {
        if (layout.getEditText() != null) {
            // Convert text to String and trim trailing spaces
            return layout.getEditText().getText().toString().trim();
        }
        return "";
    }
}