/**
 * Events Mobile Application
 *
 * File: LoginActivity.java
 *
 * Connects the activity login layout
 * to the ViewModel to manage view
 * elements effectively
 *
 * Last Modified: 2026-03-21
 *
 * Author: Raymond Bautista
 */
package com.snhu.events;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.snhu.events.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {
    private LoginViewModel viewModel;

    // UI Elements
    private TextInputLayout layoutEmailOrUser, layoutEmailOnly, layoutUsernameOnly,
            layoutPhone, layoutPassword, layoutNewPassword;
    private TextInputEditText editEmailOrUser, editEmail, editUsername,
            editPhone, editPassword, editNewPassword;
    private ConstraintLayout layoutMfaContainer;
    private TextView txtForgotPassword, txtMfaInstructions, txtMfaError;
    private MaterialButton btnLogin, btnRegister, btnSignUpToggle, btnSaveNewPassword;
    private EditText[] otpFields;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize view model
        initializeViews();
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        setupClickListeners();
        setupOtpAutoAdvance();
        observeViewModel();
    }

    // Initialize UI elements views
    private void initializeViews() {
        layoutEmailOrUser = findViewById(R.id.layoutEmailOrUser);
        layoutEmailOnly = findViewById(R.id.layoutEmailOnly);
        layoutUsernameOnly = findViewById(R.id.layoutUsernameOnly);
        layoutPhone = findViewById(R.id.layoutPhone);
        layoutPassword = findViewById(R.id.layoutPassword);
        layoutMfaContainer = findViewById(R.id.layoutMfaContainer);
        layoutNewPassword = findViewById(R.id.layoutNewPassword);

        editEmailOrUser = findViewById(R.id.editEmailOrUser);
        editEmail = findViewById(R.id.editEmail);
        editUsername = findViewById(R.id.editUsername);
        editPhone = findViewById(R.id.editPhone);
        editPassword = findViewById(R.id.editPassword);
        editNewPassword = findViewById(R.id.editNewPassword);

        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        txtMfaInstructions = findViewById(R.id.txtMfaInstructions);
        txtMfaError = findViewById(R.id.txtMfaError);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnSignUpToggle = findViewById(R.id.btnSignUpToggle);
        btnSaveNewPassword = findViewById(R.id.btnSaveNewPassword);

        otpFields = new EditText[]{
                findViewById(R.id.otp1), findViewById(R.id.otp2), findViewById(R.id.otp3),
                findViewById(R.id.otp4), findViewById(R.id.otp5), findViewById(R.id.otp6)
        };
    }

    // Setup real time on click listeners for each button
    private void setupClickListeners() {
        // Sign Up
        btnSignUpToggle.setOnClickListener(v -> viewModel.toggleSignUpMode());

        // Log In
        btnLogin.setOnClickListener(v -> {
            viewModel.login(editEmailOrUser.getText().toString(), editPassword.getText().toString());
        });

        // Forgot Password
        txtForgotPassword.setOnClickListener(v -> {
            viewModel.startPasswordRecovery(editEmailOrUser.getText().toString());
        });

        // SAVE (New Password)
        btnSaveNewPassword.setOnClickListener(v -> {
            viewModel.saveNewPassword(editNewPassword.getText().toString());
        });

        // Register (on Sign Up)
        btnRegister.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String user = editUsername.getText().toString().trim();
            String pass = editPassword.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();
            viewModel.register(email, user, pass, phone);
        });
    }

    // Toggle Elements visibility based on the mode
    private void observeViewModel() {
        viewModel.getAuthState().observe(this, state -> {
            hideAllSections();
            switch (state) {
                case LOGIN:
                    layoutEmailOrUser.setVisibility(View.VISIBLE);
                    layoutPassword.setVisibility(View.VISIBLE);
                    txtForgotPassword.setVisibility(View.VISIBLE);
                    btnLogin.setVisibility(View.VISIBLE);
                    btnSignUpToggle.setVisibility(View.VISIBLE);
                    btnSignUpToggle.setText("Don't have an account? Sign Up");
                    break;
                case SIGNUP:
                    layoutEmailOnly.setVisibility(View.VISIBLE);
                    layoutUsernameOnly.setVisibility(View.VISIBLE);
                    layoutPhone.setVisibility(View.VISIBLE);
                    layoutPassword.setVisibility(View.VISIBLE);
                    btnRegister.setVisibility(View.VISIBLE);
                    btnSignUpToggle.setVisibility(View.VISIBLE);
                    btnSignUpToggle.setText("Already have an account? Log In");
                    break;
                case MFA_LOGIN:
                    btnSignUpToggle.setVisibility(View.GONE);
                case MFA_RECOVERY:
                    btnSignUpToggle.setVisibility(View.GONE);
                    layoutMfaContainer.setVisibility(View.VISIBLE);
                    break;
                case RESET_PASSWORD:
                    layoutMfaContainer.setVisibility(View.VISIBLE);
                    layoutNewPassword.setVisibility(View.VISIBLE);
                    btnSaveNewPassword.setVisibility(View.VISIBLE);
                    break;
            }
        });

        // Get phone for MFA and status message for errors
        viewModel.getMaskedPhone().observe(this, text -> txtMfaInstructions.setText(text));
        viewModel.getStatusMessage().observe(this, msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());

        // Display MFA errors
        viewModel.getMfaErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                txtMfaError.setText(msg);
                txtMfaError.setVisibility(View.VISIBLE);
            }
        });
    }

    // Hide all elements
    private void hideAllSections() {
        View[] views = {layoutEmailOrUser, layoutEmailOnly, layoutUsernameOnly, layoutPhone,
                layoutPassword, txtForgotPassword, btnLogin, btnRegister,
                layoutMfaContainer, layoutNewPassword, btnSaveNewPassword};
        for (View v : views) v.setVisibility(View.GONE);
    }

    // Method to set up auto advance on MFA code UI elements
    private void setupOtpAutoAdvance() {
        for (int i = 0; i < otpFields.length; i++) {
            final int index = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < 5) otpFields[index + 1].requestFocus();
                    if (isOtpComplete()) viewModel.verifyMfa(getOtpString());
                }
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    // Check if the OPT Box Build UI element for MFA is completely filled
    private boolean isOtpComplete() {
        for (EditText f : otpFields) if (f.getText().length() == 0) return false;
        return true;
    }

    // Get the string on the OPT Box Build UI element for MFA
    private String getOtpString() {
        StringBuilder sb = new StringBuilder();
        for (EditText f : otpFields) sb.append(f.getText().toString());
        return sb.toString();
    }

}