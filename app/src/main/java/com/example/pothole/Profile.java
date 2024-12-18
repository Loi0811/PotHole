package com.example.pothole;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Profile extends AppCompatActivity {

    private User user;
    private TextView username, sex, birthday, address, phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ImageView back = findViewById(R.id.back);
        username = findViewById(R.id.username);
        sex = findViewById(R.id.user_sex);
        birthday = findViewById(R.id.user_birthday);
        address = findViewById(R.id.user_adress);
        phone = findViewById(R.id.user_phone);

        String nameuser= getIntent().getStringExtra("name");
        String usergender = getIntent().getStringExtra("gender");
        String userbirthday = getIntent().getStringExtra("birthday");
        String useraddress = getIntent().getStringExtra("address");
        String userphone = getIntent().getStringExtra("phone");

        username.setText(nameuser);
        sex.setText(usergender);
        birthday.setText(userbirthday);
        address.setText(useraddress);
        phone.setText(userphone);

        if (user != null) {
            username.setText(user.getName());
            sex.setText(user.getGender());
            birthday.setText(user.getBirthday());
            String user_address = user.getAddress().getDistrict() + ",\n " + user.getAddress().getProvince();
            address.setText(user_address);
            phone.setText(user.getPhone());
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}