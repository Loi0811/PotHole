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
    private ImageView avatar;

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
        avatar = findViewById(R.id.image);

        String nameuser= getIntent().getStringExtra("name");
        String usergender = getIntent().getStringExtra("gender");
        String userbirthday = getIntent().getStringExtra("birthday");
        String useraddress = getIntent().getStringExtra("address");
        String userphone = getIntent().getStringExtra("phone");
        String id_avatar = getIntent().getStringExtra("avatar");

        if (id_avatar != null){
            changeAvatar(id_avatar);
        }

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

    public void changeAvatar(String id_avatar){
        switch (id_avatar){
            case "id0":
                avatar.setImageResource(R.drawable.image);
                break;
            case "id1":
                avatar.setImageResource(R.drawable.avatar1);
                break;
            case "id2":
                avatar.setImageResource(R.drawable.avatar2);
                break;
            case "id3":
                avatar.setImageResource(R.drawable.avatar3);
                break;
            case "id4":
                avatar.setImageResource(R.drawable.avatar4);
                break;
            case "id5":
                avatar.setImageResource(R.drawable.avatar5);
                break;
            case "id6":
                avatar.setImageResource(R.drawable.avatar6);
                break;
            case "id7":
                avatar.setImageResource(R.drawable.avatar7);
                break;
            case "id8":
                avatar.setImageResource(R.drawable.avatar8);
                break;
            case "id9":
                avatar.setImageResource(R.drawable.avatar9);
                break;
            case "id10":
                avatar.setImageResource(R.drawable.avatar10);
                break;
            case "id11":
                avatar.setImageResource(R.drawable.avatar11);
                break;
            case "id12":
                avatar.setImageResource(R.drawable.avatar12);
                break;
        }
    }
}