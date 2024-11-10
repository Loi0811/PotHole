package com.example.pothole;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;

public class Home extends AppCompatActivity {
    private Fragment currentFragment;
    private Fragment dashboardFragment;
    private Fragment mapFragment;
    private Fragment historyFragment;
    private Fragment settingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize fragments
        dashboardFragment = new Dashboard();
        mapFragment = new Map();
        historyFragment = new History();
        settingFragment = new Setting();

        // Load the default fragment (Dashboard) when activity starts
        if (savedInstanceState == null) {
            currentFragment = dashboardFragment;
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, dashboardFragment)
                    .commit();
        }

        // Set up bottom navigation view
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setItemBackgroundResource(R.drawable.selected_item_background);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                if (item.getItemId() == R.id.btn_dashboard) {
                    selectedFragment = dashboardFragment;
                } else if (item.getItemId() == R.id.btn_map) {
                    selectedFragment = mapFragment;
                } else if (item.getItemId() == R.id.btn_history) {
                    selectedFragment = historyFragment;
                } else if (item.getItemId() == R.id.btn_setting) {
                    selectedFragment = settingFragment;
                }

                return switchFragment(selectedFragment);
            }
        });
    }

    private boolean switchFragment(Fragment selectedFragment) {
        if (selectedFragment != null && selectedFragment != currentFragment) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // Hide the current fragment if it exists
            if (currentFragment != null) {
                transaction.hide(currentFragment);
            }

            // Show the selected fragment, or add it if not already added
            if (!selectedFragment.isAdded()) {
                transaction.add(R.id.fragment_container, selectedFragment);
            } else {
                transaction.show(selectedFragment);
            }

            // Commit the transaction
            transaction.commit();

            // Update current fragment to the new one
            currentFragment = selectedFragment;
            return true;
        }
        return false;
    }
}