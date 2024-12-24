package com.example.pothole;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogIn extends AppCompatActivity {

    private ImageView backIcon, homeIcon, eyeIcon;
    private EditText emailField,passwordField;
    private boolean isPasswordVisible = false;
    private UserApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        apiService = ApiClient.getClient(isEmulator()).create(UserApiService.class);

        // Initialize views
        backIcon = findViewById(R.id.back);
        homeIcon = findViewById(R.id.home);
        eyeIcon = findViewById(R.id.eye);
        passwordField = findViewById(R.id.pwd);
        emailField = findViewById(R.id.user);
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
                String email = emailField.getText().toString().trim();
                String password = passwordField.getText().toString().trim();
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LogIn.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                } else {
                    loginUser(email, password);
                }
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
    private void loginUser(String email, String password) {
        Userlogin user = new Userlogin(email, password);

        apiService.loginUser(user).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isStatus()) {
                        // Successful login
                        String token = apiResponse.getToken();
                        Toast.makeText(LogIn.this, "Login successful!", Toast.LENGTH_SHORT).show();

                        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("Email", email);
                        editor.putString("Password", password);
                        editor.putBoolean("GoogleLogin", false);
                        editor.apply();

                        Intent intent = new Intent(LogIn.this, Home.class);
                        intent.putExtra("token", token);
                        startActivity(intent);
                    } else {
                        // Login failed with custom message from the response
                        Toast.makeText(LogIn.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle 401 Unauthorized (incorrect credentials)
                    if (response.code() == 401) {
                        Toast.makeText(LogIn.this, "Incorrect email or password", Toast.LENGTH_SHORT).show();
                    }
                    // Handle 404 Not Found (user not found)
                    else if (response.code() == 404) {
                        Toast.makeText(LogIn.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                    // Handle other errors
                    else {
                        Toast.makeText(LogIn.this, "Login error: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }
            }


            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("LOGIN_ERROR", "Error logging in", t);
                Toast.makeText(LogIn.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    public static boolean isEmulator() {
        return Build.FINGERPRINT.contains("generic") ||
                Build.MODEL.contains("Emulator") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                "google_sdk".equals(Build.PRODUCT);
    }
}
