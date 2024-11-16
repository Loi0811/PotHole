package com.example.pothole;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePassword extends AppCompatActivity {

    private ImageView backIcon, homeIcon, eyeIcon1, eyeIcon2;
    private EditText passwordField1, passwordField2;
    private boolean isPasswordVisible1 = false;
    private boolean isPasswordVisible2 = false;
    private Button change;
    private UserApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        apiService = ApiClient.getClient(isEmulator()).create(UserApiService.class);

        backIcon = findViewById(R.id.back);
        homeIcon = findViewById(R.id.home);
        eyeIcon1 = findViewById(R.id.eye1);
        eyeIcon2 = findViewById(R.id.eye2);
        passwordField1 = findViewById(R.id.pwd1);
        passwordField2 = findViewById(R.id.pwd2);
        change = findViewById(R.id.change);

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
                Intent intent = new Intent(ChangePassword.this, MainActivity.class);
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

        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = getIntent().getStringExtra("useremail");
                Toast.makeText(ChangePassword.this, email, Toast.LENGTH_SHORT).show();
                updatePassword(email);
            }
        });

    }

    private void showPasswordUpdatePopup(View anchorView) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.updated_password, null);

        // Create the PopupWindow
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );


        // Find the "LOG IN" button in the popup layout
        Button loginButton = popupView.findViewById(R.id.login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss(); // Close the popup
                finish();
                startActivity(new Intent(ChangePassword.this, LogIn.class));
                // Start Login Activity
            }
        });
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);
    }

    public void updatePassword(String email) {
        String newPassword1 = passwordField1.getText().toString().trim();
        String newPassword2 = passwordField2.getText().toString().trim();

        if (newPassword1.isEmpty() || newPassword2.isEmpty()) {
            Toast.makeText(ChangePassword.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword1.equals(newPassword2)) {
            Toast.makeText(ChangePassword.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        PasswordUpdateRequest request = new PasswordUpdateRequest(email, newPassword1);
        apiService.updatePassword(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse != null && apiResponse.isStatus()) {
                        Toast.makeText(ChangePassword.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                        showPasswordUpdatePopup(change);
                    } else {
                        Toast.makeText(ChangePassword.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChangePassword.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(ChangePassword.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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