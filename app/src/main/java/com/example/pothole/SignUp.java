package com.example.pothole;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pothole.Home;

public class SignUp extends AppCompatActivity {

    private ImageView backIcon, homeIcon, eyeIcon1, eyeIcon2;
    private EditText passwordField1, passwordField2;
    private boolean isPasswordVisible1 = false;
    private boolean isPasswordVisible2 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize views
        backIcon = findViewById(R.id.back);
        homeIcon = findViewById(R.id.home);
        eyeIcon1 = findViewById(R.id.eye1);
        eyeIcon2 = findViewById(R.id.eye2);
        passwordField1 = findViewById(R.id.pwd1);
        passwordField2 = findViewById(R.id.pwd2);
        Button signUpButton = findViewById(R.id.login);

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
                Intent intent = new Intent(SignUp.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        // Eye icon for passwordField1: Toggle password visibility
        eyeIcon1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible1) {
                    // Hide password
                    passwordField1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    eyeIcon1.setImageResource(R.drawable.ic_eye_foreground); // Use a closed eye icon if available
                    isPasswordVisible1 = false;
                } else {
                    // Show password
                    passwordField1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    eyeIcon1.setImageResource(R.drawable.ic_close_eye); // Use an open eye icon if available
                    isPasswordVisible1 = true;
                }
                // Move cursor to the end of the text
                passwordField1.setSelection(passwordField1.getText().length());
            }
        });

        // Eye icon for passwordField2: Toggle password visibility
        eyeIcon2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible2) {
                    // Hide password
                    passwordField2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    eyeIcon2.setImageResource(R.drawable.ic_eye_foreground); // Use a closed eye icon if available
                    isPasswordVisible2 = false;
                } else {
                    // Show password
                    passwordField2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    eyeIcon2.setImageResource(R.drawable.ic_close_eye); // Use an open eye icon if available
                    isPasswordVisible2 = true;
                }
                // Move cursor to the end of the text
                passwordField2.setSelection(passwordField2.getText().length());
            }
        });

        // Sign Up button action: Handle sign-up logic here
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, Home.class);
                startActivity(intent);
            }
        });
    }
}
