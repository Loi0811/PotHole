package com.example.pothole;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Setting extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        setupItem(R.id.logOutItem, MainActivity.class);

        ImageView backIcon = findViewById(R.id.back);
        ImageView homeIcon = findViewById(R.id.home);

        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to the previous activity
            }
        });

        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Setting.this, Home.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setupItem(int itemId, Class<?> activityClass) {
        LinearLayout item = findViewById(itemId);
        item.setOnClickListener(v -> {
            Intent intent = new Intent(Setting.this, activityClass);
            startActivity(intent);
        });
    }
}