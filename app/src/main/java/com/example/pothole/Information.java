package com.example.pothole;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public class Information extends AppCompatActivity {
    private Spinner spinnerGender;
    private TextView tvBirthday;
    private Button btnContinue;
    private Calendar calendar = Calendar.getInstance();
    private EditText etPhone, etDistrict, etProvince;
    private ImageView backIcon;
    private UserApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        apiService = ApiClient.getClient(isEmulator()).create(UserApiService.class);
        etPhone = findViewById(R.id.phone);
        spinnerGender = findViewById(R.id.gender);
        etDistrict = findViewById(R.id.district);
        etProvince = findViewById(R.id.province);
        tvBirthday = findViewById(R.id.birthday);
        btnContinue = findViewById(R.id.done);
        backIcon = findViewById(R.id.back);
        AtomicBoolean done = new AtomicBoolean(false);

        String email = getIntent().getStringExtra("sendemail");
        String name = getIntent().getStringExtra("sendname");

        tvBirthday.setOnClickListener(v -> showDatePickerDialog());
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUser(email,name);
            }
        });
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to the previous activity
            }
        });
    }

    private void showDatePickerDialog() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    String selectedDate = (month1 + 1) + "-" + dayOfMonth + "-" + year1;
                    tvBirthday.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showDoneSignup(View anchorView) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.signup_done, null);

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
                startActivity(new Intent(Information.this, LogIn.class));
                // Start Login Activity
            }
        });
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);
    }

    private void updateUser(String email, String name) {
        String phone = etPhone.getText().toString();
        String gender = spinnerGender.getSelectedItem().toString();
        String district = etDistrict.getText().toString();
        String province = etProvince.getText().toString();
        String birthday = tvBirthday.getText().toString();

        Address address = new Address();
        address.setDistrict(district);
        address.setProvince(province);

        Userupdate user = new Userupdate();
        user.setName(name);
        user.setPhone(phone);
        user.setAddress(address);
        user.setBirthday(birthday);
        user.setGender(gender);

        apiService.updateUser(email, user).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isStatus()) {
                        Toast.makeText(Information.this, "User updated successfully", Toast.LENGTH_SHORT).show();
                        showDoneSignup(btnContinue);
                    } else {
                        Toast.makeText(Information.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Information.this, "Failed to update user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(Information.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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