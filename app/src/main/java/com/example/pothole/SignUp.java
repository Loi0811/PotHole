package com.example.pothole;

import android.content.Intent;
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

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUp extends AppCompatActivity {

    private ImageView backIcon, homeIcon, eyeIcon1, eyeIcon2;
    private EditText passwordField1, passwordField2;
    private boolean isPasswordVisible1 = false;
    private boolean isPasswordVisible2 = false;
    private EditText name, email;
    private UserApiService apiService;
    private String username,useremail,pwd1,pwd2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        apiService = ApiClient.getClient(isEmulator()).create(UserApiService.class);
        backIcon = findViewById(R.id.back);
        homeIcon = findViewById(R.id.home);
        eyeIcon1 = findViewById(R.id.eye1);
        eyeIcon2 = findViewById(R.id.eye2);
        passwordField1 = findViewById(R.id.pwd1);
        passwordField2 = findViewById(R.id.pwd2);
        Button signUpButton = findViewById(R.id.signup_btn);
        TextView login = findViewById(R.id.login);
        name = findViewById(R.id.username);
        email = findViewById(R.id.user);

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
                registerUser();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, LogIn.class);
                startActivity(intent);
            }
        });
    }
    private void registerUser() {
        username = name.getText().toString().trim();
        useremail = email.getText().toString().trim();
        pwd1 = passwordField1.getText().toString().trim();
        pwd2 = passwordField2.getText().toString().trim();

        if (username.isEmpty() || useremail.isEmpty() || pwd1.isEmpty()) {
            Toast.makeText(SignUp.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pwd1.equals(pwd2)) {
            Toast.makeText(SignUp.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        String currentDate = sdf.format(new Date());

        AddressUser address = new AddressUser();
        address.setDistrict("district");
        address.setProvince("province");

        User user = new User();
        user.setName(username);
        user.setEmail(useremail);
        user.setPassword(pwd1);
        user.setPhone("phone");
        user.setAddress(address);
        user.setBirthday("2/2/2000");
        user.setGender("gender");
        user.setCteate(currentDate);

        apiService.registerUser(user).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse != null && apiResponse.isStatus()) {
                        Toast.makeText(SignUp.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignUp.this, Information.class);
                        intent.putExtra("sendemail", useremail);
                        intent.putExtra("sendname", username);
                        startActivity(intent);
                    } else {
                        Toast.makeText(SignUp.this, "Registration failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Check if the response code is 400 (Bad Request)
                    if (response.code() == 400 && response.errorBody() != null) {
                        try {
                            // Parse the error body to get the specific message
                            String errorBody = response.errorBody().string();
                            JSONObject jsonObject = new JSONObject(errorBody);
                            String errorMessage = jsonObject.optString("message", "Registration failed");

                            // Show only the error message in the Toast
                            Toast.makeText(SignUp.this, errorMessage, Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(SignUp.this, "Registration error", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SignUp.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }
            }


            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("API_ERROR", "Network error", t);
                Toast.makeText(SignUp.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
