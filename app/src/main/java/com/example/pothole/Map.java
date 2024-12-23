package com.example.pothole;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;

import retrofit2.Callback;
import retrofit2.Response;

public class Map extends Fragment implements OnMapReadyCallback,OnPotholeAddedListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private float lastAcceleration = 0.0f;  // Dùng để theo dõi sự thay đổi chiều cao (trục Z)
    private static final float THRESHOLD = 10.0f;  // Ngưỡng chiều cao thay đổi 10 cm
    private static final float GYRO_THRESHOLD = 1.0f;  // Ngưỡng thay đổi góc con quay hồi chuyển
    private boolean isDialogShowing = false;
    private Handler handler = new Handler();
    private Runnable searchRunnable;
    private AutoCompleteTextView searchLocation;
    private ImageView clearButton, runButton, stopButton, filterButton;
    private LatLng destination;
    private Polyline currentPolyline;
    private LocationCallback locationCallback;
    private UserApiService apiService;
    private double latitude, longitude;
    private String streetName,district, province;
    private static final String BASE_URL = "https://api.openrouteservice.org/";
    private static final String API_KEY = "5b3ce3597851110001cf62483f7156f213854627bb7fb7ed1304cbe1";
    private Marker userMarker = null;
    private boolean isRouteDrawn = false;
    private OpenRouteServiceApi api;
    private Marker destinationMarker1 = null;
    private Marker destinationMarker2 = null;
    private Circle userCircle = null;
    private List<PotholeClass> potholes;
    private List<Marker> markers = new ArrayList<>();
    private LinearLayout note;
    private TextView des, near_pothole;
    private String userEmail;
    private Update listener;
    private boolean isAlerting = false;
    private AddressPotholeClass old_address;
    private boolean caution_check = true;
    private boolean warning_check = true;
    private boolean danger_check = true;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Update) {
            listener = (Update) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement Update");
        }
    }


    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (!isSensorActive()) {
                return;
            }

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // Lấy giá trị gia tốc trên các trục
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float acceleration = (float) Math.sqrt(x * x + y * y + z * z);

                if (Math.abs(acceleration - lastAcceleration) > THRESHOLD) {

                    addPotholeAlert(Math.abs(acceleration - lastAcceleration));
                }
                lastAcceleration = acceleration;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // Đăng ký lắng nghe cả hai cảm biến
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
//        sensorManager.registerListener(sensorEventListener, gyroscope, SensorManager.SENSOR_DELAY_UI);
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        apiService = ApiClient.getClient(isEmulator()).create(UserApiService.class);
        api = RetrofitClient.getInstance(BASE_URL).create(OpenRouteServiceApi.class);

        searchLocation = view.findViewById(R.id.search);
        clearButton = view.findViewById(R.id.clear);
        runButton = view.findViewById(R.id.run);
        stopButton = view.findViewById(R.id.stop);
        filterButton = view.findViewById(R.id.filter);

        note = view.findViewById(R.id.note);
        des = view.findViewById(R.id.destination);
        near_pothole = view.findViewById(R.id.pothole_nearest);


        Home homeActivity = (Home) getActivity();
        if (homeActivity != null) {
            potholes = homeActivity.getPotholeList();
            userEmail = homeActivity.getUseremail();
        }

        clearButton.setOnClickListener(v -> {
            searchLocation.setText("");
            clearButton.setVisibility(View.GONE);
        });

        runButton.setOnClickListener(v -> {
            if (destination != null) {
                // Reset trạng thái đã vẽ đường
                drawRouteToDestination(destination);
            } else {
                Toast.makeText(getActivity(), "Vui lòng chọn điểm đến", Toast.LENGTH_SHORT).show();
            }
        });

        stopButton.setOnClickListener(v -> {
            cancelRoute();
            stopButton.setVisibility(View.GONE);
        });

        filterButton.setOnClickListener(v -> {

            View dialogView = getLayoutInflater().inflate(R.layout.filter_dialog, null);

            CheckBox checkBoxCation = dialogView.findViewById(R.id.checkbox_caution);
            CheckBox checkBoxWarning = dialogView.findViewById(R.id.checkbox_warning);
            CheckBox checkBoxDanger = dialogView.findViewById(R.id.checkbox_danger);
            Spinner timeSpinner = dialogView.findViewById(R.id.time_spinner);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(),
                    android.R.layout.simple_spinner_item,
                    new String[]{"7 Days", "1 Month", "1 Year", "All Time"});
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            timeSpinner.setAdapter(adapter);


            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();

            checkBoxCation.setChecked(caution_check);
            checkBoxWarning.setChecked(warning_check);
            checkBoxDanger.setChecked(danger_check);

            Button btnApply = dialogView.findViewById(R.id.btn_apply);
            btnApply.setOnClickListener(applyView -> {
                boolean cation = checkBoxCation.isChecked();
                caution_check = checkBoxCation.isChecked();
                boolean warning = checkBoxWarning.isChecked();
                warning_check = checkBoxWarning.isChecked();
                boolean danger = checkBoxDanger.isChecked();
                danger_check = checkBoxDanger.isChecked();
                String selectedTime = (String) timeSpinner.getSelectedItem();

                applyFilters(cation, warning, danger, selectedTime);

                dialog.dismiss();
            });

            Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
            btnCancel.setOnClickListener(cancelView -> dialog.dismiss());

            dialog.show();

            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        });

        searchLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    fetchSuggestionsWithDelay(query, searchLocation);
                }
                clearButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        // Initialize the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        ImageView jumpToMyLocationButton = view.findViewById(R.id.my_location);
        jumpToMyLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();
            }
        });

        getLastLocation();

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View view = getLayoutInflater().inflate(R.layout.custom_info_window, null);

                TextView title = view.findViewById(R.id.title);
                TextView snippet = view.findViewById(R.id.snippet);

                title.setText(marker.getTitle());
                snippet.setText(marker.getSnippet());


                return view;
            }

        });

        mMap.setOnInfoWindowClickListener(marker -> {
            showUpdateDialog(marker);
        });

        mMap.setOnMapLongClickListener(latLng -> {
            showLongPressOptions(latLng);
        });

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }


        addMarkers();

        getCurrentLocation();


    }

    @Override
    public void onPotholeAdded(PotholeClass newPothole) {
        potholes.add(newPothole);
        LatLng location = new LatLng(newPothole.getLatitude(), newPothole.getLongitude());
        String snippet = newPothole.getAddressPothole().getDistrict() + "," + newPothole.getAddressPothole().getProvince()
                + "\n" + newPothole.getDate() + "\n" +
                "Type:" + getDangerLevel(newPothole.getType());

        // Tạo marker và thêm vào bản đồ
        MarkerOptions markerOptions = new MarkerOptions()
                .position(location)
                .title("Pothole: " + newPothole.getAddressPothole().getStreetName())
                .snippet(snippet)
                .icon(getMarkerColor(newPothole.getType()));

        Marker marker = mMap.addMarker(markerOptions);

        if (marker != null) {
            marker.setTag(newPothole);
            markers.add(marker);
        }
    }

    private void showLongPressOptions(LatLng latLng) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Choose action")
                .setMessage("What do you want to do at this position?")
                .setPositiveButton("Add Pothole", (dialog, which) -> {
                    addPotholeHuman(latLng);

                })
                .setNeutralButton("Direction", (dialog, which) -> {
                    // Gọi hàm điều hướng đến vị trí
                    destination = latLng;
                    String value = "Destination";
                    if (destinationMarker1 != null) {
                        destinationMarker1.remove();
                    }

                    destinationMarker1 = mMap.addMarker(new MarkerOptions()
                            .position(destination)
                            .title(value));
                    des.setText(value);
                    drawRouteToDestination(destination);
                    stopButton.setVisibility(View.VISIBLE);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addPotholeAlert(float acceleration) {
        Dialog dialog = new Dialog(requireActivity());
        dialog.setContentView(R.layout.detect_pothole);

        if (isDialogShowing) {
            return;
        }

        isDialogShowing = true;

        TextView level = dialog.findViewById(R.id.level);
        Button yes = dialog.findViewById(R.id.yes);
        Button no = dialog.findViewById(R.id.no);
        int type;

        if (acceleration > 17.0) {
            level.setText("Danger");
            level.setTextColor(getResources().getColor(R.color.risk_red));
            type = 3;
        } else if (acceleration > 15.0) {
            level.setText("Warning");
            type = 2;
            level.setTextColor(getResources().getColor(R.color.risk_orange));
        } else {
            type = 1;
            if (acceleration > 10.0) {
                level.setText("Caution");
                level.setTextColor(getResources().getColor(R.color.risk_yellow));
            }
        }

        yes.setOnClickListener(v->{
            getLastLocation();
            getAddressFromLocation(latitude, longitude);
            AddressPothole addressPothole = new AddressPothole();
            AddressPotholeClass addressPotholeClass = new AddressPotholeClass();
            addressPothole.setStreetName(streetName);
            addressPothole.setDistrict(district);
            addressPothole.setProvince(province);

            addressPotholeClass.setStreetName(streetName);
            addressPotholeClass.setDistrict(district);
            addressPotholeClass.setProvince(province);

            Pothole pothole = new Pothole();
            PotholeClass potholeClass = new PotholeClass();
            pothole.setAddressPothole(addressPothole);
            pothole.setLatitude(latitude);
            pothole.setLongitude(longitude);
            pothole.setDate(getCurrentTime());
            pothole.setType(type);
            pothole.setAuthor(userEmail);

            potholeClass.setAddressPothole(addressPotholeClass);
            potholeClass.setLatitude(latitude);
            potholeClass.setLongitude(longitude);
            potholeClass.setDate(getCurrentTime());
            potholeClass.setType(type);
            potholeClass.setAuthor(userEmail);

            addPothole(pothole);

            potholes.add(potholeClass);

            if (listener != null){
                listener.DataAfterAdd(potholeClass);
                addHistory(potholeClass, potholeClass.getType(),potholeClass.getType(),potholeClass.getAddressPothole(),potholeClass.getAddressPothole(),"ADD POTHOLE");
            }

            LatLng location = new LatLng(latitude, longitude);

            String snippet = pothole.getAddressPothole().getDistrict() + "," + pothole.getAddressPothole().getProvince()
                    + "\n" + pothole.getDate() + "\n" +
                    "Type:" + getDangerLevel(pothole.getType());

            // Tạo marker và thêm vào bản đồ
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(location)
                    .title("Pothole: " + pothole.getAddressPothole().getStreetName())
                    .snippet(snippet)
                    .icon(getMarkerColor(pothole.getType()));

            Marker marker = mMap.addMarker(markerOptions);

            if (marker != null) {
                marker.setTag(pothole);
                markers.add(marker);
            }
            dialog.dismiss();
            isDialogShowing = false;
        });

        no.setOnClickListener(v->{
            dialog.dismiss();
            isDialogShowing = false;
        });

        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Xóa nền mặc định
        }
    }

    private void addPotholeHuman(LatLng latLng){
        Dialog dialog = new Dialog(requireActivity());
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



        String[] potholeTypes = {"Caution", "Warning", "Danger"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item, potholeTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);

        latitudeEditText.setText(String.valueOf(latLng.latitude));
        longitudeEditText.setText(String.valueOf(latLng.longitude));

        String latitudeText = latitudeEditText.getText().toString();
        String longitudeText = longitudeEditText.getText().toString();
        latitude = Double.parseDouble(latitudeText);
        longitude = Double.parseDouble(longitudeText);

        geocoderbtn.setOnClickListener(v3 -> {
            updateAddressFromCoordinates(latitudeEditText,longitudeEditText,streetTextView,districtTextView,provinceTextView);
        });

        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                streetTextView.setText(address.getThoroughfare());
                districtTextView.setText(address.getSubAdminArea());
                provinceTextView.setText(address.getAdminArea());
            } else {
                Toast.makeText(getActivity(), "Cannot fetch address!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error getting address!", Toast.LENGTH_SHORT).show();
        }

        cancelButton.setOnClickListener(v1 -> dialog.dismiss());

        addButton.setOnClickListener(v2 -> {
            String selectedType = typeSpinner.getSelectedItem().toString();
            int type = 0;

            if ("Caution".equals(selectedType)) type = 1;
            else if ("Warning".equals(selectedType)) type = 2;
            else if ("Danger".equals(selectedType)) type = 3;

            if (type == 0) {
                Toast.makeText(getActivity(), "Please select a pothole type", Toast.LENGTH_SHORT).show();
                return;
            }


            AddressPothole addressPothole = new AddressPothole();
            addressPothole.setStreetName(streetTextView.getText().toString());
            addressPothole.setDistrict(districtTextView.getText().toString());
            addressPothole.setProvince(provinceTextView.getText().toString());

            Pothole pothole = new Pothole();
            pothole.setType(type);
            pothole.setLatitude(latitude);
            pothole.setLongitude(longitude);
            pothole.setDate(getCurrentTime());
            pothole.setAuthor(userEmail);
            pothole.setAddressPothole(addressPothole);

            AddressPotholeClass addressPotholeClass = new AddressPotholeClass();
            addressPotholeClass.setStreetName(streetTextView.getText().toString());
            addressPotholeClass.setDistrict(districtTextView.getText().toString());
            addressPotholeClass.setProvince(provinceTextView.getText().toString());

            PotholeClass newPothole = new PotholeClass();
            newPothole.setType(type);
            newPothole.setLatitude(latitude);
            newPothole.setLongitude(longitude);
            newPothole.setDate(getCurrentTime());
            newPothole.setAuthor(userEmail);
            newPothole.setAddressPothole(addressPotholeClass);

            apiService.addPothole(pothole).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse apiResponse = response.body();
                        if (apiResponse.isStatus()) {
                            potholes.add(newPothole);
                            if (listener != null) {
                                Log.d("MAP","Send_data");
                                listener.DataAfterAdd(newPothole);
                                addHistory(newPothole, newPothole.getType(), newPothole.getType(), newPothole.getAddressPothole(),newPothole.getAddressPothole(),"ADD POTHOLE");
                            }
                             String snippet = newPothole.getAddressPothole().getDistrict() + "," + newPothole.getAddressPothole().getProvince()
                                    + "\n" + newPothole.getDate() + "\n" +
                                    "Type:" + getDangerLevel(newPothole.getType());

                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(latLng)
                                    .title("Pothole: " + newPothole.getAddressPothole().getStreetName())
                                    .snippet(snippet)
                                    .icon(getMarkerColor(newPothole.getType()));

                            Marker marker = mMap.addMarker(markerOptions);

                            if (marker != null) {
                                marker.setTag(newPothole);
                                markers.add(marker);
                            }

                            Toast.makeText(getActivity(), "Pothole added successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (response.code() == 400) {
                            Toast.makeText(getActivity(), "Pothole already exists at the same location", Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 404) {
                            Toast.makeText(getActivity(), "User or address not found", Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 500) {
                            Toast.makeText(getActivity(), "Server error. Please try again later.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Failed to add pothole: " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Log.e("POTHOLE_ERROR", "Error adding pothole", t);
                    Toast.makeText(getActivity(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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

            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
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
            Toast.makeText(getActivity(), "Invalid coordinates", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getActivity(), "Error fetching address", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void showUpdateDialog(Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        View dialogView = getLayoutInflater().inflate(R.layout.update_pothole, null);
        builder.setView(dialogView);

        EditText street = dialogView.findViewById(R.id.street);
        EditText district = dialogView.findViewById(R.id.district);
        EditText province = dialogView.findViewById(R.id.province);
        Spinner typeSpinner = dialogView.findViewById(R.id.type);
        TextView latitude = dialogView.findViewById(R.id.latitude);
        TextView longitude = dialogView.findViewById(R.id.longitude);
        Button btnUpdate = dialogView.findViewById(R.id.update);
        Button btnDelete = dialogView.findViewById(R.id.delete);
        ImageView btnCancel = dialogView.findViewById(R.id.cancel);
        TextView date = dialogView.findViewById(R.id.date);

        PotholeClass pothole = (PotholeClass) marker.getTag();
        if (pothole != null) {
            street.setText(pothole.getAddressPothole().getStreetName());
            district.setText(pothole.getAddressPothole().getDistrict());
            province.setText(pothole.getAddressPothole().getProvince());
            old_address = pothole.getAddressPothole();
            latitude.setText(String.valueOf(marker.getPosition().latitude));
            longitude.setText(String.valueOf(marker.getPosition().longitude));
            date.setText(pothole.getDate());
            String[] potholeTypes = {"Caution", "Warning", "Danger"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, potholeTypes);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            typeSpinner.setAdapter(adapter);

            String currentType = getDangerLevel(pothole.getType());
            int position = Arrays.asList(potholeTypes).indexOf(currentType);
            if (position >= 0) {
                typeSpinner.setSelection(position);
            }
        }
        AlertDialog dialog = builder.create();

        btnUpdate.setOnClickListener(v -> {
            if (pothole != null) {
                String selectedType = typeSpinner.getSelectedItem().toString();
                int type = 0;
                int old_type = pothole.getType();

                if ("Caution".equals(selectedType)) type = 1;
                else if ("Warning".equals(selectedType)) type = 2;
                else if ("Danger".equals(selectedType)) type = 3;
                pothole.getAddressPothole().setStreetName(street.getText().toString());
                pothole.getAddressPothole().setDistrict(district.getText().toString());
                pothole.getAddressPothole().setProvince(province.getText().toString());
                AddressPotholeClass new_address = pothole.getAddressPothole();
                pothole.setType(type);


                String action = "UPDATE POTHOLE";
                if ((old_type != type) && (old_address != new_address)){
                    action = "UPDATE POTHOLE(ADDRESS,TYPE)";
                } else if (old_type != type) {
                    action = "UPDATE POTHOLE(TYPE)";
                } else if (old_address != new_address) {
                    action = "UPDATE POTHOLE(ADDRESS)";
                } else {
                    dialog.dismiss();
                    return;
                }
                updatePothole(pothole,old_type,type,old_address,new_address,action);

                for (int i = 0; i < potholes.size(); i++) {
                    PotholeClass currentPothole = potholes.get(i);
                    if (currentPothole.getLatitude() == marker.getPosition().latitude && currentPothole.getLongitude() == marker.getPosition().longitude) {
                        potholes.set(i, pothole);
                        break;
                    }
                }

                marker.setTitle("Pothole: " + pothole.getAddressPothole().getStreetName());
                marker.setSnippet(district.getText().toString() + "," + province.getText().toString() +
                        "\n" + pothole.getDate() +
                        "\nType: " + getDangerLevel(pothole.getType()));
                marker.setIcon(getMarkerColor(pothole.getType()));
                marker.showInfoWindow();
            }
            dialog.dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            marker.remove();
            if (pothole != null){
                Double lat = marker.getPosition().latitude;
                Double lon = marker.getPosition().longitude;
                String author = pothole.getAuthor();
                Integer type_remove = pothole.getType();
                String date_remove = pothole.getDate();

                deletePothole(pothole,lat,lon,author,type_remove, date_remove);

                for (int i = 0; i < potholes.size(); i++) {
                    PotholeClass currentPothole = potholes.get(i);
                    if (currentPothole.getLatitude() == marker.getPosition().latitude && currentPothole.getLongitude() == marker.getPosition().longitude) {
                        potholes.remove(i);
                        break;
                    }
                }
            }
            dialog.dismiss();
        });



        btnCancel.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();

            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.CENTER;

            window.setAttributes(params);

            window.setBackgroundDrawable(new ColorDrawable(0xF4F4F4));
        }

    }


    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permissions if not granted
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17));
                    } else {
                        Toast.makeText(getActivity(), "Unable to find current location.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permissions if not granted
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), location -> {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                });
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getActivity());
        try {
            // Lấy danh sách địa chỉ từ tọa độ
            List<Address> addressUsers = geocoder.getFromLocation(latitude, longitude, 1);

            if (addressUsers != null && !addressUsers.isEmpty()) {
                Address address = addressUsers.get(0);

                // Lấy các phần thông tin của địa chỉ
                streetName = address.getThoroughfare(); // Địa chỉ đường phố
                district = address.getSubLocality(); // Quận/Huyện
                province = address.getAdminArea(); // Tỉnh/Thành phố

            } else {
                Toast.makeText(getActivity(), "Không tìm thấy địa chỉ cho tọa độ này.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Lỗi khi lấy địa chỉ.", Toast.LENGTH_SHORT).show();
        }
    }

    public String getCurrentTime() {
        // Tạo đối tượng SimpleDateFormat với định dạng mong muốn
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        // Lấy thời gian hiện tại
        Date date = new Date();

        // Định dạng thời gian hiện tại và trả về kết quả
        return dateFormat.format(date);
    }

    private void addMarkers() {
        // Example markers list
        if (potholes != null) {
            for (Marker marker : markers) {
                marker.remove();
            }
            markers.clear();
            for (PotholeClass pothole : potholes) {
                double latitude = pothole.getLatitude();
                double longitude = pothole.getLongitude();
                LatLng location = new LatLng(latitude, longitude);

                String snippet = pothole.getAddressPothole().getDistrict() + "," + pothole.getAddressPothole().getProvince()
                        + "\n" + pothole.getDate() + "\n" +
                        "Type:" + getDangerLevel(pothole.getType());

                // Tạo marker và thêm vào bản đồ
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(location)
                        .title("Pothole: " + pothole.getAddressPothole().getStreetName())
                        .snippet(snippet)
                        .icon(getMarkerColor(pothole.getType())); // Chọn màu marker dựa trên loại ổ gà

                Marker marker = mMap.addMarker(markerOptions);

                if (marker != null) {
                    marker.setTag(pothole);
                    markers.add(marker);
                }
            }
        }
    }

    private String getDangerLevel(int type) {
        switch (type) {
            case 1:
                return "Caution";
            case 2:
                return "Warning";
            case 3:
                return "Danger";
            default:
                return "Unknown";
        }
    }

    // Phương thức chọn màu sắc của marker theo type
    private BitmapDescriptor getMarkerColor(int type) {
        BitmapDescriptor icon;
        switch (type) {
            case 1:
                icon = BitmapFromVector(getActivity(),R.drawable.caution_sign);
                return icon;
            case 2:
                icon = BitmapFromVector(getActivity(),R.drawable.warning_sign);
                return icon;
            case 3:
                icon = BitmapFromVector(getActivity(),R.drawable.danger_sign);
                return icon;
            default:
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN); // Mặc định là màu xanh
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(getActivity(), "Permission denied to access location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchSuggestionsWithDelay(String query, AutoCompleteTextView searchBox) {
        if (searchRunnable != null) {
            handler.removeCallbacks(searchRunnable);
        }

        searchRunnable = () -> {
            fetchSuggestions(query, searchBox);
        };
        handler.postDelayed(searchRunnable, 500); // Delay 500ms
    }


    private void fetchSuggestions(String query, AutoCompleteTextView searchBox) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }

        Call<GeocodeResponse> call = api.searchLocation(API_KEY, query, 5);

        call.enqueue(new Callback<GeocodeResponse>() {
            @Override
            public void onResponse(Call<GeocodeResponse> call, Response<GeocodeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GeocodeResponse.Feature> locations = response.body().features;
                    List<String> suggestions = new ArrayList<>();
                    for (GeocodeResponse.Feature location : locations) {
                        suggestions.add(location.properties.label);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            getActivity(),
                            android.R.layout.simple_dropdown_item_1line,
                            suggestions
                    );
                    searchBox.setAdapter(adapter);
                    searchBox.showDropDown();

                    searchBox.setOnItemClickListener((parent, view, position, id) -> {
                        GeocodeResponse.Feature selectedLocation = locations.get(position);
                        destination = new LatLng(selectedLocation.geometry.coordinates.get(1),
                                selectedLocation.geometry.coordinates.get(0));

                        if (destinationMarker1 != null) {
                            destinationMarker1.remove();
                        }

                        destinationMarker1 = mMap.addMarker(new MarkerOptions()
                                .position(destination)
                                .title(selectedLocation.properties.label));
                        des.setText(selectedLocation.properties.label);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destination, 17));
                    });

                } else {
                    Toast.makeText(getActivity(), "No suggestions found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GeocodeResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "Failed to fetch suggestions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void drawRouteToDestination(LatLng destination) {
        // Kiểm tra quyền
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
            return;
        }

        // Khởi tạo fusedLocationClient
        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        requestRoute(currentLocation, destination);

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17));

                        PotholeClass nearestPothole = getNearestPothole(currentLocation);
                        if (nearestPothole != null) {
                            double distanceToNearest = calculateDistance(currentLocation, new LatLng(nearestPothole.getLatitude(), nearestPothole.getLongitude()));
                            int value = (int) distanceToNearest;
                            String data = "Nearest pothole: " + value + "m";
                            near_pothole.setText(data);
                        } else {
                            near_pothole.setText("No pothole");
                        }
                    } else {
                        Toast.makeText(getActivity(), "Unable to find current location.", Toast.LENGTH_SHORT).show();
                    }
                });



        LocationRequest routeLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(2000)
                .setFastestInterval(1000);

        LocationCallback routeLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                Log.d("LocationCallback", "Location update received.");

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    Log.d("LocationCallback", "Current LatLng: " + newLatLng);

                    if(isRouteDrawn){
                        PotholeClass nearestPothole = getNearestPothole(newLatLng);
                        if (nearestPothole != null) {
                            double distanceToNearest = calculateDistance(newLatLng, new LatLng(nearestPothole.getLatitude(), nearestPothole.getLongitude()));


                            int value = (int) distanceToNearest;
                            String data = "Nearest pothole: " + value + "m";
                            near_pothole.setText(data);

                            if (distanceToNearest <= 30) {
                                near_pothole.setTextColor(getResources().getColor(R.color.risk_red));

                                animateTextViewShake(near_pothole);
                            } else {
                                near_pothole.setTextColor(getResources().getColor(R.color.dark_gray));
                            }

                            if (distanceToNearest <= 30 && !isAlerting) {
                                isAlerting = true;
                                if (isSoundEnabled()) playSound();
                                if (isVibrationEnabled()) vibratePhone();

                                new Handler(Looper.getMainLooper()).postDelayed(() -> isAlerting = false, 1500);
                            }

                            if (distanceToNearest <= 10) {
                                markers.removeIf(marker -> {
                                    if (marker.getTag() == nearestPothole) {
                                        marker.remove();
                                        return true;
                                    }
                                    return false;
                                });
                                Toast.makeText(requireContext(), "Pothole passed!", Toast.LENGTH_SHORT).show();
                                isAlerting = false;
                                near_pothole.setTextColor(getResources().getColor(R.color.dark_gray));
                            }
                        } else {
                            near_pothole.setText("No pothole");
                            isAlerting = false;
                            near_pothole.setTextColor(getResources().getColor(R.color.dark_gray));
                        }
                    } else {
                        fusedLocationClient.removeLocationUpdates(this);
                    }

                    if (isUserAtDestination(newLatLng, destination)) {
                        fusedLocationClient.removeLocationUpdates(this);
                        isRouteDrawn = false;
                        Toast.makeText(requireContext(), "You have arrived at your destination!", Toast.LENGTH_SHORT).show();
                        cancelRoute();
                    }
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(routeLocationRequest, routeLocationCallback, Looper.getMainLooper());


        // Khởi tạo locationCallback
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(2000) // Cập nhật mỗi 2 giây
                .setFastestInterval(1000);

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if(isRouteDrawn){
                        PotholeClass nearestPothole = getNearestPothole(newLatLng);
                        if (nearestPothole != null) {
                            double distanceToNearest = calculateDistance(newLatLng, new LatLng(nearestPothole.getLatitude(), nearestPothole.getLongitude()));


                            int value = (int) distanceToNearest;
                            String data = "Nearest pothole: " + value + "m";
                            near_pothole.setText(data);

                            if (distanceToNearest <= 30) {
                                near_pothole.setTextColor(getResources().getColor(R.color.risk_red));

                                animateTextViewShake(near_pothole);
                            } else {
                                near_pothole.setTextColor(getResources().getColor(R.color.dark_gray));
                            }

                            if (distanceToNearest <= 30 && !isAlerting) {
                                isAlerting = true;
                                if (isSoundEnabled()) playSound();
                                if (isVibrationEnabled()) vibratePhone();

                                new Handler(Looper.getMainLooper()).postDelayed(() -> isAlerting = false, 1500);
                            }

                            if (distanceToNearest <= 10) {
                                markers.removeIf(marker -> {
                                    if (marker.getTag() == nearestPothole) {
                                        marker.remove();
                                        return true;
                                    }
                                    return false;
                                });
                                Toast.makeText(requireContext(), "Pothole passed!", Toast.LENGTH_SHORT).show();
                                isAlerting = false;
                                near_pothole.setTextColor(getResources().getColor(R.color.dark_gray));
                            }
                        } else {
                            near_pothole.setText("No pothole");
                            isAlerting = false;
                            near_pothole.setTextColor(getResources().getColor(R.color.dark_gray));
                        }
                    }

                    if (isUserAtDestination(newLatLng, destination)) {
                        fusedLocationClient.removeLocationUpdates(this);
                        isRouteDrawn = false;
                        Toast.makeText(requireContext(), "You have arrived at your destination!", Toast.LENGTH_SHORT).show();
                        cancelRoute();
                    }
                    if(!isRouteDrawn)
                    {
                        fusedLocationClient.removeLocationUpdates(this);
                    }

                }
            }
        }, Looper.getMainLooper());

    }

    private double calculateDistance(LatLng from, LatLng to) {
        float[] results = new float[1];
        Location.distanceBetween(from.latitude, from.longitude, to.latitude, to.longitude, results);
        return results[0]; // Khoảng cách tính bằng mét
    }

    private PotholeClass getNearestPothole(LatLng currentLocation) {
        double minDistance = Double.MAX_VALUE;
        PotholeClass nearestPothole = null;

        for (Marker marker : markers) {
            PotholeClass pothole = (PotholeClass) marker.getTag();
            if (pothole != null) {
                double distance = calculateDistance(currentLocation, new LatLng(pothole.getLatitude(), pothole.getLongitude()));
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestPothole = pothole;
                }
            }
        }
        return nearestPothole;
    }

    private void playSound() {
        MediaPlayer mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alert_sound);
        mediaPlayer.start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        }, 1500);
    }


    private void vibratePhone() {
        Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(1500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(1500);
            }
        }
    }

    private void animateTextViewShake(TextView textView) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(textView, "translationX", 0, 10, -10, 10, -10, 5, -5, 0);
        animator.setDuration(500); // Thời gian lắc 500ms
        animator.start();
    }



    private boolean isSoundEnabled() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return prefs.getBoolean("sound", true);
    }

    private boolean isVibrationEnabled() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return prefs.getBoolean("vibration", true);
    }

    private boolean isSensorActive() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return prefs.getBoolean("sensor", true);
    }

    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);

        vectorDrawable.setBounds(
                0, 0, vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight()
        );

        Bitmap bitmap = Bitmap.createBitmap(
                vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    private boolean isUserAtDestination(LatLng userLocation, LatLng destination) {
        float[] results = new float[1];
        Location.distanceBetween(
                userLocation.latitude, userLocation.longitude,
                destination.latitude, destination.longitude,
                results
        );
        return results[0] <= 10;
    }

    private void requestRoute(LatLng startLatLng, LatLng destination) {
        String start = startLatLng.longitude + "," + startLatLng.latitude;
        String end = destination.longitude + "," + destination.latitude;

        if (destinationMarker1 != null){
            clearMarker(destinationMarker2);
            LatLng position = destinationMarker1.getPosition();
            String title = destinationMarker1.getTitle();
            destinationMarker2 = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(title));
            clearMarker(destinationMarker1);
        }


        Call<RouteResponse> call = api.getRoute(API_KEY, start, end);
        call.enqueue(new Callback<RouteResponse>() {
            @Override
            public void onResponse(Call<RouteResponse> call, Response<RouteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RouteResponse routeResponse = response.body();
                    List<double[]> coordinates = routeResponse.getFeatures().get(0).getGeometry().getCoordinates();
                    PolylineOptions polylineOptions = new PolylineOptions().width(10).color(0xFF0000FF).geodesic(true);
                    List<LatLng> routePoints = new ArrayList<>();

                    for (double[] coord : coordinates) {
                        LatLng latLng = new LatLng(coord[1], coord[0]);
                        polylineOptions.add(latLng);
                        routePoints.add(latLng);
                    }

                    // Vẽ đường trên bản đồ
                    if (currentPolyline != null) {
                        currentPolyline.remove();
                    }
                    currentPolyline = mMap.addPolyline(polylineOptions);
                    isRouteDrawn = true;

                    addMarkersOnRoute(routePoints);
                    stopButton.setVisibility(View.VISIBLE);
                    note.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getActivity(), "Không thể lấy thông tin đường đi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RouteResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "Yêu cầu thất bại: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMarkersOnRoute(List<LatLng> routePoints) {
        for (Marker marker : markers) {
            marker.remove();
        }
        markers.clear();
        if (potholes != null && !routePoints.isEmpty()) {
            for (PotholeClass pothole : potholes) {
                LatLng potholeLocation = new LatLng(pothole.getLatitude(), pothole.getLongitude());

                if (isNearRoute(potholeLocation, routePoints)) {
                    String snippet = pothole.getAddressPothole().getDistrict() + "," + pothole.getAddressPothole().getProvince()
                            + "\n" + pothole.getDate() + "\n" +
                            "Type:" + getDangerLevel(pothole.getType());

                    // Tạo marker và thêm vào bản đồ
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(potholeLocation)
                            .title("Pothole: " + pothole.getAddressPothole().getStreetName())
                            .snippet(snippet)
                            .icon(getMarkerColor(pothole.getType()));

                    Marker marker = mMap.addMarker(markerOptions);

                    if (marker != null) {
                        marker.setTag(pothole);
                        markers.add(marker);
                    }
                }
            }
        }
    }

    private boolean isNearRoute(LatLng potholeLocation, List<LatLng> routePoints) {
        if (routePoints == null || routePoints.size() < 2) {
            return false;
        }

        for (int i = 0; i < routePoints.size() - 1; i++) {
            LatLng start = routePoints.get(i);
            LatLng end = routePoints.get(i + 1);

            double distance = distanceToSegment(potholeLocation, start, end);
            if (distance <= 5) {
                return true;
            }
        }

        return false;
    }


    private double distanceToSegment(LatLng point, LatLng start, LatLng end) {
        double px = point.latitude;
        double py = point.longitude;
        double sx = start.latitude;
        double sy = start.longitude;
        double ex = end.latitude;
        double ey = end.longitude;

        double dx = ex - sx;
        double dy = ey - sy;

        if (dx == 0 && dy == 0) {
            return distanceBetween(point, start);
        }

        double t = ((px - sx) * dx + (py - sy) * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t));

        double nearestX = sx + t * dx;
        double nearestY = sy + t * dy;

        return distanceBetween(point, new LatLng(nearestX, nearestY));
    }

    private double distanceBetween(LatLng point1, LatLng point2) {
        float[] results = new float[1];
        Location.distanceBetween(
                point1.latitude, point1.longitude,
                point2.latitude, point2.longitude,
                results
        );
        return results[0];
    }



    private void clearMarker(Marker destinationMarker) {
        if (destinationMarker != null) {
            destinationMarker.remove();
            destinationMarker = null;
        }
    }

    private void addPothole(Pothole pothole) {

        Call<ApiResponse> call = apiService.addPothole(pothole);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isStatus()) {
                        Toast.makeText(getActivity(), "Pothole added successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (response.code() == 400) {
                        Toast.makeText(getActivity(), "Pothole already exists at the same location", Toast.LENGTH_SHORT).show();
                    } else if (response.code() == 404) {
                        Toast.makeText(getActivity(), "User or address not found", Toast.LENGTH_SHORT).show();
                    } else if (response.code() == 500) {
                        Toast.makeText(getActivity(), "Server error. Please try again later.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Failed to add pothole: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("POTHOLE_ERROR", "Error adding pothole", t);
                Toast.makeText(getActivity(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePothole(PotholeClass pothole, Integer old_type, Integer new_type, AddressPotholeClass old_address, AddressPotholeClass new_address, String action) {

        Call<PotholeResponse> call = apiService.updatePothole(pothole);
        call.enqueue(new Callback<PotholeResponse>() {
            @Override
            public void onResponse(Call<PotholeResponse> call, Response<PotholeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getActivity(), "Update successful", Toast.LENGTH_SHORT).show();
                    if (listener != null){
                        listener.DataAfterUpdate(old_type,new_type);
                        addHistory(pothole, old_type, new_type, old_address, new_address,action);
                    }
                } else {
                    Toast.makeText(getActivity(), "Update failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PotholeResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deletePothole(PotholeClass potholeClass, Double lat, Double lon, String author, Integer type, String date) {

        Call<PotholeResponse> call = apiService.deletePothole(lat, lon, author);
        call.enqueue(new Callback<PotholeResponse>() {
            @Override
            public void onResponse(Call<PotholeResponse> call, Response<PotholeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getActivity(), "Delete successful", Toast.LENGTH_SHORT).show();
                    if (listener != null){
                        listener.DataAfterDelete(type,date);
                        addHistory(potholeClass, type, type, potholeClass.getAddressPothole(), potholeClass.getAddressPothole(), "DELETE POTHOLE");
                    }
                } else {
                    Toast.makeText(getActivity(), "Delete failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PotholeResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addHistory(PotholeClass potholeClass, Integer old_type, Integer new_type, AddressPotholeClass old_address, AddressPotholeClass new_address, String action){
        Integer point = 0;
        if (action.equals("ADD POTHOLE")){
            point = potholeClass.getType()*2 - 1;
        } else if (action.equals("UPDATE POTHOLE(ADDRESS)")||action.equals("UPDATE POTHOLE(TYPE)")||action.equals("UPDATE POTHOLE(ADDRESS,TYPE)")){
            point = new_type*2 - old_type*2;
        } else {
            point = 1 - potholeClass.getType()*2;
        }

        HistoryClass history = new HistoryClass();
        history.setAction(action);
        history.setAddressPothole(new_address);
        history.setAuthor(potholeClass.getAuthor());
        history.setDate(getCurrentTime());
        history.setType(potholeClass.getType());
        history.setLatitude(potholeClass.getLatitude());
        history.setLongitude(potholeClass.getLongitude());
        history.setPoint(point);

        apiService.addHistory(history).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isStatus()) {
                        Toast.makeText(getActivity(), "History added successfully!", Toast.LENGTH_SHORT).show();
                        if (listener != null){
                            listener.AddHistory(history);
                        }
                    } else {
                        Toast.makeText(getActivity(), apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (response.code() == 500) {
                        Toast.makeText(getActivity(), "Server error. Please try again later.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Failed to add history: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("POTHOLE_ERROR", "Error adding pothole", t);
                Toast.makeText(getActivity(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void cancelRoute() {
        if (currentPolyline != null) {
            currentPolyline.remove();
            currentPolyline= null;
            isRouteDrawn =false;
            mMap.setOnMapClickListener(null);
            destinationMarker2.remove();
            destinationMarker2 = null;
            destination = null;
            addMarkers();
            note.setVisibility(View.GONE);
        } else {
            Toast.makeText(requireContext(), "No route to cancel", Toast.LENGTH_SHORT).show();
        }
    }

    private void applyFilters(boolean caution, boolean warning, boolean danger, String selectedTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault());
        long currentTime = System.currentTimeMillis();
        for (Marker marker : markers) {
            PotholeClass potholeClass = (PotholeClass) marker.getTag();
            if (potholeClass != null){
                Integer type = potholeClass.getType();

                boolean matchesSeverity = (caution && (type == 1)) ||
                        (warning && (type == 2)) ||
                        (danger && (type == 3));

                Date reportDate = parseDate(potholeClass.getDate());
                if (reportDate == null) continue;



                boolean matchesTime = false;
                if (selectedTime.equals("7 Days")) {
                    matchesTime = currentTime - reportDate.getTime() <= 7L * 24 * 60 * 60 * 1000;
                } else if (selectedTime.equals("1 Month")) {
                    matchesTime = currentTime - reportDate.getTime() <= 30L * 24 * 60 * 60 * 1000;
                } else if (selectedTime.equals("1 Year")) {
                    matchesTime = currentTime - reportDate.getTime() <= 365L * 24 * 60 * 60 * 1000;
                } else if (selectedTime.equals("All Time")) {
                    matchesTime = true;
                }
                marker.setVisible(matchesSeverity && matchesTime);
            }
        }
    }

    private Date parseDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault());
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isEmulator() {
        return Build.FINGERPRINT.contains("generic") ||
                Build.MODEL.contains("Emulator") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                "google_sdk".equals(Build.PRODUCT);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null; // Hủy liên kết khi Fragment bị tháo
    }

    public interface Update {
        void DataAfterAdd(PotholeClass potholeClass);
        void DataAfterUpdate(Integer old_type, Integer new_type);
        void DataAfterDelete(Integer type, String Date);
        void AddHistory(HistoryClass historyClass);
    }
}
