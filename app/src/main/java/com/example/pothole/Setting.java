package com.example.pothole;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Setting extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        setupItem(R.id.logOutItem, MainActivity.class);

        ImageView homeIcon = findViewById(R.id.home);

        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Setting.this, Home.class);
                startActivity(intent);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set the default selected item (optional)
        bottomNavigationView.setSelectedItemId(R.id.btn_setting);

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.btn_home){
                    startActivity(new Intent(Setting.this, Home.class));
                    return true;
                } else if (itemId == R.id.btn_history){
                    startActivity(new Intent(Setting.this, History.class));
                    return true;
                } else if (itemId == R.id.btn_map) {
                    startActivity(new Intent(Setting.this, Map.class));
                    return true;
                }
                return false;
            }
        });
    }

    private void setupItem(int itemId, Class<?> activityClass) {
        LinearLayout item = findViewById(itemId);
        item.setOnClickListener(v -> {
            Intent intent = new Intent(Setting.this, activityClass);
            if (itemId == R.id.logOutItem) intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
    }
}