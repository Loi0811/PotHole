package com.example.pothole;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

public class Information extends AppCompatActivity {
    private Spinner spinnerGender;
    private TextView tvBirthday;
    private Button btnContinue;
    private Calendar calendar = Calendar.getInstance();
    private EditText etPhone, etDistrict, etProvince;
    private ImageView backIcon,avatar;
    private UserApiService apiService;
    private Boolean google;
    private String id_avatar;

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
        avatar = findViewById(R.id.image);
        AtomicBoolean done = new AtomicBoolean(false);

        String email = getIntent().getStringExtra("sendemail");
        String name = getIntent().getStringExtra("sendname");
        google = getIntent().getBooleanExtra("google",false);

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
                finish();
            }
        });
        avatar.setOnClickListener(v->{
            showAvatarDialog();
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

    private void showAvatarDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.avatar);
        dialog.setCancelable(true);

        ImageView back = dialog.findViewById(R.id.back);
        back.setOnClickListener(v->{
            dialog.dismiss();
        });

        ImageView avatar1 = dialog.findViewById(R.id.avatar1);
        avatar1.setOnClickListener(v->{
            id_avatar = "id1";
            avatar.setImageResource(R.drawable.avatar1);
            dialog.dismiss();
        });

        ImageView avatar2 = dialog.findViewById(R.id.avatar2);
        avatar2.setOnClickListener(v->{
            id_avatar = "id2";
            avatar.setImageResource(R.drawable.avatar2);
            dialog.dismiss();
        });
        ImageView avatar3 = dialog.findViewById(R.id.avatar3);
        avatar3.setOnClickListener(v->{
            id_avatar = "id3";
            avatar.setImageResource(R.drawable.avatar3);
            dialog.dismiss();
        });

        ImageView avatar4 = dialog.findViewById(R.id.avatar4);
        avatar4.setOnClickListener(v->{
            id_avatar = "id4";
            avatar.setImageResource(R.drawable.avatar4);
            dialog.dismiss();
        });
        ImageView avatar5 = dialog.findViewById(R.id.avatar5);
        avatar5.setOnClickListener(v->{
            id_avatar = "id5";
            avatar.setImageResource(R.drawable.avatar5);
            dialog.dismiss();
        });

        ImageView avatar6 = dialog.findViewById(R.id.avatar6);
        avatar6.setOnClickListener(v->{
            id_avatar = "id6";
            avatar.setImageResource(R.drawable.avatar6);
            dialog.dismiss();
        });

        ImageView avatar7 = dialog.findViewById(R.id.avatar7);
        avatar7.setOnClickListener(v->{
            id_avatar = "id7";
            avatar.setImageResource(R.drawable.avatar7);
            dialog.dismiss();
        });

        ImageView avatar8 = dialog.findViewById(R.id.avatar8);
        avatar8.setOnClickListener(v->{
            id_avatar = "id8";
            avatar.setImageResource(R.drawable.avatar8);
            dialog.dismiss();
        });

        ImageView avatar9 = dialog.findViewById(R.id.avatar9);
        avatar9.setOnClickListener(v->{
            id_avatar = "id9";
            avatar.setImageResource(R.drawable.avatar9);
            dialog.dismiss();
        });

        ImageView avatar10 = dialog.findViewById(R.id.avatar10);
        avatar10.setOnClickListener(v->{
            id_avatar = "id10";
            avatar.setImageResource(R.drawable.avatar10);
            dialog.dismiss();
        });

        ImageView avatar11 = dialog.findViewById(R.id.avatar11);
        avatar11.setOnClickListener(v->{
            id_avatar = "id11";
            avatar.setImageResource(R.drawable.avatar11);
            dialog.dismiss();
        });

        ImageView avatar12 = dialog.findViewById(R.id.avatar12);
        avatar12.setOnClickListener(v->{
            id_avatar = "id12";
            avatar.setImageResource(R.drawable.avatar12);
            dialog.dismiss();
        });


        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setGravity(Gravity.CENTER);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Hiển thị dialog
        dialog.show();
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


        Button loginButton = popupView.findViewById(R.id.login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                finish();
                startActivity(new Intent(Information.this, LogIn.class));
                SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("Email");
                editor.apply();
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

        AddressUser address = new AddressUser();
        address.setDistrict(district);
        address.setProvince(province);

        Userupdate user = new Userupdate();
        user.setName(name);
        user.setPhone(phone);
        user.setAddress(address);
        user.setBirthday(birthday);
        user.setGender(gender);
        user.setAvatar(id_avatar);

        apiService.updateUser(email, user).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isStatus()) {
                        Toast.makeText(Information.this, "User updated successfully", Toast.LENGTH_SHORT).show();
                        if (google){
                            startActivity(new Intent(Information.this, Home.class));
                        } else {
                            showDoneSignup(btnContinue);
                        }
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