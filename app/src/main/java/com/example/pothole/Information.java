package com.example.pothole;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class Information extends AppCompatActivity {
    private Spinner spinnerGender;
    private TextView tvBirthday;
    private Button btnSave, btnSkip;
    private Calendar calendar = Calendar.getInstance();
    private ImageView backIcon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        spinnerGender = findViewById(R.id.gender);
        tvBirthday = findViewById(R.id.birthday);
        btnSave = findViewById(R.id.save);
        btnSkip = findViewById(R.id.skip);
        backIcon = findViewById(R.id.back);
        tvBirthday.setOnClickListener(v -> showDatePickerDialog());
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordUpdatePopup(v);
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordUpdatePopup(v);
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
                    calendar.set(year1, month1, dayOfMonth);
                    String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                    tvBirthday.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showPasswordUpdatePopup(View anchorView) {
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
}