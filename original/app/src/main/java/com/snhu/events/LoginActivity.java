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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.snhu.events.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {
    private LoginViewModel viewModel;
    private TextInputLayout layoutEmailOrUser, layoutEmailOnly, layoutUsernameOnly, layoutPhone, layoutPassword;
    private MaterialButton btnLogin, btnRegister, btnSignUpToggle;
    private TextView txtError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        initViews();

        // Toggle Visibility based on Mode
        viewModel.getIsSignUpMode().observe(this, isSignUp -> {
            layoutEmailOrUser.setVisibility(isSignUp ? View.GONE : View.VISIBLE);
            layoutEmailOnly.setVisibility(isSignUp ? View.VISIBLE : View.GONE);
            layoutUsernameOnly.setVisibility(isSignUp ? View.VISIBLE : View.GONE);
            layoutPhone.setVisibility(isSignUp ? View.VISIBLE : View.GONE);
            btnLogin.setVisibility(isSignUp ? View.GONE : View.VISIBLE);
            btnRegister.setVisibility(isSignUp ? View.VISIBLE : View.GONE);
            btnSignUpToggle.setText(isSignUp ? "Back to Log In" : "Sign Up");
        });

        // Observes User object and saves ID to SharedPreferences
        viewModel.getAuthenticatedUser().observe(this, user -> {
            if (user != null) {
                SharedPreferences prefs = getSharedPreferences("EventPrefs", MODE_PRIVATE);
                prefs.edit().putInt("USER_ID", user.id).apply();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                txtError.setText(error);
                txtError.setVisibility(View.VISIBLE);
            } else {
                txtError.setVisibility(View.GONE);
            }
        });

        View.OnClickListener submitListener = v -> viewModel.submitForm(
                getText(layoutEmailOrUser), getText(layoutEmailOnly),
                getText(layoutPassword), getText(layoutUsernameOnly), getText(layoutPhone)
        );

        btnLogin.setOnClickListener(submitListener);
        btnRegister.setOnClickListener(submitListener);
        btnSignUpToggle.setOnClickListener(v -> viewModel.toggleMode());
    }

    private void initViews() {
        layoutEmailOrUser = findViewById(R.id.layoutEmailOrUser);
        layoutEmailOnly = findViewById(R.id.layoutEmailOnly);
        layoutUsernameOnly = findViewById(R.id.layoutUsernameOnly);
        layoutPhone = findViewById(R.id.layoutPhone);
        layoutPassword = findViewById(R.id.layoutPassword);
        txtError = findViewById(R.id.txtError);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnSignUpToggle = findViewById(R.id.btnSignUpToggle);
    }

    private String getText(TextInputLayout layout) {
        return (layout.getEditText() != null) ? layout.getEditText().getText().toString().trim() : "";
    }
}