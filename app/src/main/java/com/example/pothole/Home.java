package com.example.pothole;

import android.Manifest;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;

import retrofit2.Callback;
import retrofit2.Response;

public class Home extends AppCompatActivity implements Map.UpdateChart, Setting.UserSetting {
    private Fragment currentFragment;
    private Fragment dashboardFragment;
    private Fragment mapFragment;
    private Fragment historyFragment;
    private Fragment settingFragment;
    private double latitude, longitude;
    private UserApiService apiService;
    private FusedLocationProviderClient fusedLocationClient;
    private List<PotholeClass> potholes = new ArrayList<>();
    private String useremail;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        apiService = ApiClient.getClient(isEmulator()).create(UserApiService.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        useremail = sharedPreferences.getString("Email", null);
        fetchPotholesByAuthor(useremail);
//        fetchUserByEmail(useremail);

        ImageView addReport = findViewById(R.id.add_report);
        dashboardFragment = new Dashboard();
        mapFragment = new Map();
        historyFragment = new History();
        settingFragment = new Setting();

        addReport.setOnClickListener(v -> {
            // Tạo dialog
            getLastLocation();
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.add_pothole);

            // Lấy các thành phần từ dialog
            Spinner typeSpinner = dialog.findViewById(R.id.type);
            EditText streetTextView = dialog.findViewById(R.id.street);
            EditText districtTextView = dialog.findViewById(R.id.district);
            EditText provinceTextView = dialog.findViewById(R.id.province);
            EditText latitudeEditText = dialog.findViewById(R.id.latitude);
            EditText longitudeEditText = dialog.findViewById(R.id.longitude);
            Button cancelButton = dialog.findViewById(R.id.cancel);
            Button addButton = dialog.findViewById(R.id.add);
            ImageView geocoderbtn = dialog.findViewById(R.id.geocoder);



            // Thiết lập dữ liệu cho Spinner
            String[] potholeTypes = {"Caution", "Warning", "Danger"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, potholeTypes);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            typeSpinner.setAdapter(adapter);

            // Lấy tọa độ hiện tại và hiển thị lên giao diện
            latitudeEditText.setText(String.valueOf(latitude));
            longitudeEditText.setText(String.valueOf(longitude));

            geocoderbtn.setOnClickListener(v3 -> {
                updateAddressFromCoordinates(latitudeEditText,longitudeEditText,streetTextView,districtTextView,provinceTextView);
            });



            // Dùng Geocoder để lấy địa chỉ từ tọa độ
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    streetTextView.setText(address.getThoroughfare());
                    districtTextView.setText(address.getSubAdminArea());
                    provinceTextView.setText(address.getAdminArea());
                } else {
                    Toast.makeText(this, "Cannot fetch address!", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error getting address!", Toast.LENGTH_SHORT).show();
            }

            // Xử lý nút Cancel
            cancelButton.setOnClickListener(v1 -> dialog.dismiss());

            // Xử lý nút Add
            addButton.setOnClickListener(v2 -> {
                String selectedType = typeSpinner.getSelectedItem().toString();
                int type = 0;

                if ("Caution".equals(selectedType)) type = 1;
                else if ("Warning".equals(selectedType)) type = 2;
                else if ("Danger".equals(selectedType)) type = 3;

                if (type == 0) {
                    Toast.makeText(this, "Please select a pothole type", Toast.LENGTH_SHORT).show();
                    return;
                }

                AddressPothole addressPothole = new AddressPothole();
                addressPothole.setStreetName(streetTextView.getText().toString());
                addressPothole.setDistrict(districtTextView.getText().toString());
                addressPothole.setProvince(provinceTextView.getText().toString());

                // Tạo đối tượng Pothole
                Pothole pothole = new Pothole();
                pothole.setType(type);
                pothole.setLatitude(latitude);
                pothole.setLongitude(longitude);
                pothole.setDate(getCurrentDateTime());
                pothole.setAuthor(useremail);
                pothole.setAddressPothole(addressPothole);

                AddressPotholeClass addressPotholeClass = new AddressPotholeClass();
                addressPotholeClass.setStreetName(streetTextView.getText().toString());
                addressPotholeClass.setDistrict(districtTextView.getText().toString());
                addressPotholeClass.setProvince(provinceTextView.getText().toString());

                PotholeClass newPothole = new PotholeClass();
                newPothole.setType(type);
                newPothole.setLatitude(latitude);
                newPothole.setLongitude(longitude);
                newPothole.setDate(getCurrentDateTime());
                newPothole.setAuthor(useremail);
                newPothole.setAddressPothole(addressPotholeClass);



                // Gửi API để thêm ổ gà
                apiService.addPothole(pothole).enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse apiResponse = response.body();
                            if (apiResponse.isStatus()) {
                                Fragment mapFragment = (Fragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);

                                if (mapFragment instanceof OnPotholeAddedListener) {
                                    ((OnPotholeAddedListener) mapFragment).onPotholeAdded(newPothole);
                                }
                                potholes.add(newPothole);
                                Dashboard dashboard = (Dashboard) getSupportFragmentManager().findFragmentByTag("DashboardFragment");
                                if (dashboard != null){
                                    dashboard.ChartAfterAdd(newPothole);
                                }


                                // Thêm ổ gà thành công
                                Toast.makeText(Home.this, "Pothole added successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                // Thêm ổ gà thất bại với thông báo lỗi từ server
                                Toast.makeText(Home.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Xử lý các lỗi với mã trạng thái
                            if (response.code() == 400) {
                                Toast.makeText(Home.this, "Pothole already exists at the same location", Toast.LENGTH_SHORT).show();
                            } else if (response.code() == 404) {
                                Toast.makeText(Home.this, "User or address not found", Toast.LENGTH_SHORT).show();
                            } else if (response.code() == 500) {
                                Toast.makeText(Home.this, "Server error. Please try again later.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Home.this, "Failed to add pothole: " + response.message(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        // Lỗi mạng hoặc lỗi ngoài mong đợi
                        Log.e("POTHOLE_ERROR", "Error adding pothole", t);
                        Toast.makeText(Home.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.dismiss();
            });

            dialog.show();
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setGravity(Gravity.CENTER);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Xóa nền mặc định
            }
        });


        // Initialize fragments


        // Load the default fragment (Dashboard) when activity starts
        if (savedInstanceState == null) {
            currentFragment = dashboardFragment;
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, dashboardFragment, "DashboardFragment")
                    .commit();
        }

        // Set up bottom navigation view
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setItemBackgroundResource(R.drawable.selected_item_background);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                String tag = null;

                if (item.getItemId() == R.id.btn_dashboard) {
                    selectedFragment = dashboardFragment;
                    tag = "DashboardFragment";
                    Dashboard dashboard = (Dashboard) getSupportFragmentManager().findFragmentByTag("DashboardFragment");
                    if (dashboard != null) {
                        user = dashboard.getUser();
                    }
                } else if (item.getItemId() == R.id.btn_map) {
                    selectedFragment = mapFragment;
                    tag = "MapFragment";
                } else if (item.getItemId() == R.id.btn_history) {
                    selectedFragment = historyFragment;
                    tag = "HistoryFragment";
                } else if (item.getItemId() == R.id.btn_setting) {
                    selectedFragment = settingFragment;
                    tag = "SettingFragment";
                    Dashboard dashboard = (Dashboard) getSupportFragmentManager().findFragmentByTag("DashboardFragment");
                    if (dashboard != null) {
                        user = dashboard.getUser();
                    }
                }

                return switchFragment(selectedFragment, tag);
            }
        });


    }

    private boolean switchFragment(Fragment selectedFragment, String tag) {
        if (selectedFragment != null && selectedFragment != currentFragment) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // Hide the current fragment
            if (currentFragment != null) {
                transaction.hide(currentFragment);
            }

            // Check if the selected fragment is already added
            Fragment fragmentByTag = getSupportFragmentManager().findFragmentByTag(tag);
            if (fragmentByTag == null) {
                transaction.add(R.id.fragment_container, selectedFragment, tag);
            } else {
                transaction.show(fragmentByTag);
            }

            // Commit the transaction
            transaction.commit();

            // Update current fragment
            currentFragment = selectedFragment;
            return true;
        }
        return false;
    }


    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.getDefault());
        return sdf.format(new Date());
    }
    public static boolean isEmulator() {
        return Build.FINGERPRINT.contains("generic") ||
                Build.MODEL.contains("Emulator") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                "google_sdk".equals(Build.PRODUCT);
    }
    private void updateAddressFromCoordinates(EditText latitudeEditText, EditText longitudeEditText,
                                              EditText streetTextView, EditText districtTextView, EditText provinceTextView) {
        String latitudeText = latitudeEditText.getText().toString();
        String longitudeText = longitudeEditText.getText().toString();

        // Kiểm tra giá trị hợp lệ
        if (latitudeText.isEmpty() || longitudeText.isEmpty()) {
            streetTextView.setText("");
            districtTextView.setText("");
            provinceTextView.setText("");
            return;
        }

        try {
            latitude = Double.parseDouble(latitudeText);
            longitude = Double.parseDouble(longitudeText);

            // Lấy địa chỉ bằng Geocoder
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                streetTextView.setText(address.getThoroughfare() != null ? address.getThoroughfare() : "N/A");
                districtTextView.setText(address.getSubAdminArea() != null ? address.getSubAdminArea() : "N/A");
                provinceTextView.setText(address.getAdminArea() != null ? address.getAdminArea() : "N/A");
            } else {
                streetTextView.setText("Unknown");
                districtTextView.setText("Unknown");
                provinceTextView.setText("Unknown");
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid coordinates", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error fetching address", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(Home.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(Home.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permissions if not granted
            ActivityCompat.requestPermissions(Home.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(Home.this, location -> {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                });
    }

    private void fetchPotholesByAuthor(String authorEmail) {
        apiService.getPotholes(authorEmail).enqueue(new Callback<PotholeResponse>() {
            @Override
            public void onResponse(Call<PotholeResponse> call, Response<PotholeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PotholeResponse potholeResponse = response.body();
                    potholes = potholeResponse.getPotholes();
                    Toast.makeText(Home.this, "OK", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Home.this, "Failed to load potholes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PotholeResponse> call, Throwable t) {
                Toast.makeText(Home.this, "Error fetching potholes: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserByEmail(String email) {
        Call<ApiResponse> call = apiService.getUserByEmail(email);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body().isStatus()) {
                    user = response.body().getData();
                    if (user != null) {
                        Log.d("User Name", user.getName());
                        Toast.makeText(Home.this, user.getName(), Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("Error", "User is null");
                    }
                } else {
                    Log.e("Error", response.body() != null ? response.body().getMessage() : "Unknown error");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("API Error", t.getMessage());
            }
        });
    }

    public List<PotholeClass> getPotholeList() {
        return potholes;
    }
    public String getUseremail() {
        return useremail;
    }
    public User getUser(){
        return user;
    }

    @Override
    public void DataChartAfterAdd (PotholeClass potholeClass){
        Dashboard dashboard = (Dashboard) getSupportFragmentManager().findFragmentByTag("DashboardFragment");
        if (dashboard != null){
            Log.d("HOME","Give data");
            dashboard.ChartAfterAdd(potholeClass);
        }
    }

    @Override
    public void DataChartAfterUpdate (Integer old_type, Integer new_type){
        Dashboard dashboard = (Dashboard) getSupportFragmentManager().findFragmentByTag("DashboardFragment");
        if (dashboard != null){
            dashboard.ChartAfterUpdate(old_type,new_type);
        }
    }

    @Override
    public void DataChartAfterDelete (Integer type, String date){
        Dashboard dashboard = (Dashboard) getSupportFragmentManager().findFragmentByTag("DashboardFragment");
        if (dashboard != null){
            dashboard.ChartAfterDelete(type,date);
        }
    }

    @Override
    public void SetUser (User user){
        Dashboard dashboard = (Dashboard) getSupportFragmentManager().findFragmentByTag("DashboardFragment");
        if (dashboard != null){
            dashboard.ChangeUser(user);
        }
    }
}