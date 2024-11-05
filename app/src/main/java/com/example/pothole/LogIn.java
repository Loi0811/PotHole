package com.example.pothole;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class LogIn extends AppCompatActivity {

    private ImageView backIcon, homeIcon, eyeIcon;
    private EditText passwordField;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        // Initialize views
        backIcon = findViewById(R.id.back);
        homeIcon = findViewById(R.id.home);
        eyeIcon = findViewById(R.id.eye);
        passwordField = findViewById(R.id.pwd);
        Button loginButton = findViewById(R.id.login_btn);
        TextView forgotPassword = findViewById(R.id.forgotpwd);
        TextView signup = findViewById(R.id.signup);

        // Back icon action: Navigate to the previous activity
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to the previous activity
            }
        });

        // Home icon action: Navigate to the MainActivity
        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogIn.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        // Eye icon action: Toggle password visibility
        eyeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // Hide password
                    passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    eyeIcon.setImageResource(R.drawable.ic_eye_foreground); // Use a closed eye icon if available
                    isPasswordVisible = false;
                } else {
                    // Show password
                    passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    eyeIcon.setImageResource(R.drawable.ic_close_eye); // Use an open eye icon if available
                    isPasswordVisible = true;
                }
                // Move cursor to the end of the text
                passwordField.setSelection(passwordField.getText().length());
            }
        });

        // Login button action: Handle login logic here
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogIn.this, Home.class);
                startActivity(intent);
            }
        });

        // Forgot password action: Handle forgot password logic
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogIn.this, ForgotPassword.class);
                startActivity(intent);
                finish();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogIn.this, SignUp.class);
                startActivity(intent);
            }
        });
    }
}
