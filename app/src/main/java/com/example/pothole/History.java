package com.example.pothole;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class History extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ListView listview = findViewById(R.id.list);
        ArrayList<HistoryItem> arrayList = new ArrayList<>();
        arrayList.add(new HistoryItem(R.drawable.pothole, "ADD REPORT", "Nguyen Du", "Di An", "Binh Duong", 10.88, 106.78, 10, 45, 2, 11, 2024, 3));
        arrayList.add(new HistoryItem(R.drawable.pothole, "ADD REPORT", "Nguyen Du", "Di An", "Binh Duong", 10.88, 106.78, 17, 45, 1, 11, 2024, 2));
        arrayList.add(new HistoryItem(R.drawable.pothole, "ADD REPORT", "Nguyen Du", "Di An", "Binh Duong", 10.88, 106.78, 10, 45, 1, 11, 2024, 3));
        arrayList.add(new HistoryItem(R.drawable.pothole, "UPDATE REPORT", "Nguyen Du", "Di An", "Binh Duong", 10.88, 106.78, 8, 45, 1, 11, 2024, 1));
        arrayList.add(new HistoryItem(R.drawable.pothole, "ADD REPORT", "Nguyen Du", "Di An", "Binh Duong", 10.88, 106.78, 10, 45, 29, 10, 2024, 3));
        arrayList.add(new HistoryItem(R.drawable.pothole, "ADD REPORT", "Nguyen Du", "Di An", "Binh Duong", 10.88, 106.78, 17, 45, 24, 10, 2024, 1));
        arrayList.add(new HistoryItem(R.drawable.pothole, "ADD REPORT", "Nguyen Du", "Di An", "Binh Duong", 10.88, 106.78, 10, 45, 20, 10, 2024, 3));
        arrayList.add(new HistoryItem(R.drawable.pothole, "UPDATE REPORT", "Nguyen Du", "Di An", "Binh Duong", 10.88, 106.78, 8, 45, 19, 10, 2024, 2));
        arrayList.add(new HistoryItem(R.drawable.pothole, "ADD REPORT", "Nguyen Du", "Di An", "Binh Duong", 10.88, 106.78, 10, 45, 2, 9, 2024, 1));
        arrayList.add(new HistoryItem(R.drawable.pothole, "ADD REPORT", "Nguyen Du", "Di An", "Binh Duong", 10.88, 106.78, 17, 45, 1, 9, 2024, 2));
        arrayList.add(new HistoryItem(R.drawable.pothole, "ADD REPORT", "Nguyen Du", "Di An", "Binh Duong", 10.88, 106.78, 10, 45, 1, 9, 2024, 3));
        arrayList.add(new HistoryItem(R.drawable.pothole, "UPDATE REPORT", "Nguyen Du", "Di An", "Binh Duong", 10.88, 106.78, 8, 45, 1, 9, 2024, 3));

        AdapterHistory adapter = new AdapterHistory(History.this, R.layout.item_history, arrayList);
        listview.setAdapter(adapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set the default selected item (optional)
        bottomNavigationView.setSelectedItemId(R.id.btn_history);

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.btn_map){
                    startActivity(new Intent(History.this, Map.class));
                    return true;
                } else if (itemId == R.id.btn_home){
                    startActivity(new Intent(History.this, Home.class));
                    return true;
                } else if (itemId == R.id.btn_profile) {
                    startActivity(new Intent(History.this, Profile.class));
                    return true;
                }
                return false;
            }
        });
    }
}