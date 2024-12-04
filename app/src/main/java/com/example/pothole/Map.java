package com.example.pothole;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
    private float lastZ = 0.0f;  // Dùng để theo dõi sự thay đổi chiều cao (trục Z)
    private static final float THRESHOLD = 10.0f;  // Ngưỡng chiều cao thay đổi 10 cm
    private static final float GYRO_THRESHOLD = 1.0f;  // Ngưỡng thay đổi góc con quay hồi chuyển
    private boolean isDialogShowing = false;
    private Handler handler = new Handler();
    private Runnable searchRunnable;
    private AutoCompleteTextView searchLocation;
    private ImageView clearButton, runButton, stopButton;
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


    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // Lấy giá trị gia tốc trên trục Z (chiều cao)
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                // Kiểm tra sự thay đổi chichi cao (10 cm)
                if (Math.abs(z - lastZ) > THRESHOLD) {
                    // Nếu có sự thay đổi chiều cao lớn hơn 10cm
                    showAddPotholeDialog(x, y, z);  // Gọi dialog để thêm ổ gà
                }
                lastZ = z;

            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                // Lấy dữ liệu từ cảm biến con quay hồi chuyển
                float deltaX = event.values[0];  // Tốc độ quay trên trục X
                float deltaY = event.values[1];  // Tốc độ quay trên trục Y
                float deltaZ = event.values[2];  // Tốc độ quay trên trục Z

                // Kiểm tra sự thay đổi góc (quay) vượt ngưỡng
                if (Math.abs(deltaX) > GYRO_THRESHOLD || Math.abs(deltaY) > GYRO_THRESHOLD || Math.abs(deltaZ) > GYRO_THRESHOLD) {
                    // Nếu có sự thay đổi góc lớn hơn ngưỡng
                    showAddPotholeDialog(deltaX, deltaY, deltaZ);  // Gọi dialog để thêm ổ gà
                }
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
                // Không tùy chỉnh toàn bộ cửa sổ InfoWindow
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View view = getLayoutInflater().inflate(R.layout.custom_info_window, null);

                // Ánh xạ các thành phần trong layout
                TextView title = view.findViewById(R.id.title);
                TextView snippet = view.findViewById(R.id.snippet);

                // Lấy dữ liệu từ marker và hiển thị
                title.setText(marker.getTitle());
                snippet.setText(marker.getSnippet());


                return view;
            }

        });

        mMap.setOnInfoWindowClickListener(marker -> {
            // Mở dialog hoặc thực hiện hành động
            showUpdateDialog(marker);
        });

        mMap.setOnMapLongClickListener(latLng -> {
            showLongPressOptions(latLng);
        });


        // Add markers
        addMarkers();

        // Get current location and move camera to it
        getCurrentLocation();


    }

    @Override
    public void onPotholeAdded(PotholeClass newPothole) {
        // Thêm ổ gà mới vào danh sách
        potholes.add(newPothole);
        // Thêm marker mới lên bản đồ
        LatLng location = new LatLng(newPothole.getLatitude(), newPothole.getLongitude());
        String snippet = newPothole.getAddressPothole().getDistrict() + "," + newPothole.getAddressPothole().getProvince()
                + "\n" + newPothole.getDate() + "\n" +
                "Type:" + getDangerLevel(newPothole.getType());

        // Tạo marker và thêm vào bản đồ
        MarkerOptions markerOptions = new MarkerOptions()
                .position(location)
                .title("Pothole: " + newPothole.getAddressPothole().getStreetName())
                .snippet(snippet)
                .icon(getMarkerColor(newPothole.getType())); // Chọn màu marker dựa trên loại ổ gà

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

        // Hiển thị hộp thoại
        AlertDialog dialog = builder.create();
        dialog.show();
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



        // Thiết lập dữ liệu cho Spinner
        String[] potholeTypes = {"Caution", "Warning", "Danger"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item, potholeTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);

        // Lấy tọa độ hiện tại và hiển thị lên giao diện
        latitudeEditText.setText(String.valueOf(latLng.latitude));
        longitudeEditText.setText(String.valueOf(latLng.longitude));

        String latitudeText = latitudeEditText.getText().toString();
        String longitudeText = longitudeEditText.getText().toString();
        latitude = Double.parseDouble(latitudeText);
        longitude = Double.parseDouble(longitudeText);

        geocoderbtn.setOnClickListener(v3 -> {
            updateAddressFromCoordinates(latitudeEditText,longitudeEditText,streetTextView,districtTextView,provinceTextView);
        });



        // Dùng Geocoder để lấy địa chỉ từ tọa độ
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
                Toast.makeText(getActivity(), "Please select a pothole type", Toast.LENGTH_SHORT).show();
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



            // Gửi API để thêm ổ gà
            apiService.addPothole(pothole).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse apiResponse = response.body();
                        if (apiResponse.isStatus()) {
                            potholes.add(newPothole);
                            String snippet = newPothole.getAddressPothole().getDistrict() + "," + newPothole.getAddressPothole().getProvince()
                                    + "\n" + newPothole.getDate() + "\n" +
                                    "Type:" + getDangerLevel(newPothole.getType());

                            // Tạo marker và thêm vào bản đồ
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(latLng)
                                    .title("Pothole: " + newPothole.getAddressPothole().getStreetName())
                                    .snippet(snippet)
                                    .icon(getMarkerColor(newPothole.getType())); // Chọn màu marker dựa trên loại ổ gà

                            Marker marker = mMap.addMarker(markerOptions);

                            if (marker != null) {
                                marker.setTag(newPothole);
                                markers.add(marker);
                            }

                            // Thêm ổ gà thành công
                            Toast.makeText(getActivity(), "Pothole added successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            // Thêm ổ gà thất bại với thông báo lỗi từ server
                            Toast.makeText(getActivity(), apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Xử lý các lỗi với mã trạng thái
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
                    // Lỗi mạng hoặc lỗi ngoài mong đợi
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

            // Lấy địa chỉ bằng Geocoder
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
        // Tạo dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        View dialogView = getLayoutInflater().inflate(R.layout.update_pothole, null);
        builder.setView(dialogView);

        // Ánh xạ các thành phần trong dialog
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

        // Lấy dữ liệu từ marker hoặc đối tượng gắn trong tag
        PotholeClass pothole = (PotholeClass) marker.getTag();
        if (pothole != null) {
            street.setText(pothole.getAddressPothole().getStreetName());
            district.setText(pothole.getAddressPothole().getDistrict());
            province.setText(pothole.getAddressPothole().getProvince());
            latitude.setText(String.valueOf(marker.getPosition().latitude));
            longitude.setText(String.valueOf(marker.getPosition().longitude));
            date.setText(pothole.getDate());
            // Chọn đúng loại trong Spinner
            String[] potholeTypes = {"Caution", "Warning", "Danger"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, potholeTypes);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            typeSpinner.setAdapter(adapter);

            // Đặt giá trị loại ổ gà hiện tại
            String currentType = getDangerLevel(pothole.getType());
            int position = Arrays.asList(potholeTypes).indexOf(currentType);
            if (position >= 0) {
                typeSpinner.setSelection(position);
            }
        }
        AlertDialog dialog = builder.create();

        // Xử lý khi nhấn "Update"
        btnUpdate.setOnClickListener(v -> {
            // Cập nhật dữ liệu cho Pothole
            if (pothole != null) {
                String selectedType = typeSpinner.getSelectedItem().toString();
                int type = 0;

                if ("Caution".equals(selectedType)) type = 1;
                else if ("Warning".equals(selectedType)) type = 2;
                else if ("Danger".equals(selectedType)) type = 3;
                pothole.getAddressPothole().setStreetName(street.getText().toString());
                pothole.getAddressPothole().setDistrict(district.getText().toString());
                pothole.getAddressPothole().setProvince(province.getText().toString());
                pothole.setType(type);

                updatePothole(pothole);

                for (int i = 0; i < potholes.size(); i++) {
                    PotholeClass currentPothole = potholes.get(i);
                    if (currentPothole.getLatitude() == marker.getPosition().latitude && currentPothole.getLongitude() == marker.getPosition().longitude) {
                        potholes.set(i, pothole);
                        break;
                    }
                }

                // Cập nhật lại marker
                marker.setTitle("Pothole: " + pothole.getAddressPothole().getStreetName());
                marker.setSnippet(district.getText().toString() + "," + province.getText().toString() +
                        "\n" + pothole.getDate() +
                        "\nType: " + getDangerLevel(pothole.getType()));
                marker.setIcon(getMarkerColor(pothole.getType()));
                marker.showInfoWindow(); // Hiển thị lại InfoWindow
            }
            dialog.dismiss();
        });

        // Xử lý khi nhấn "Delete"
        btnDelete.setOnClickListener(v -> {
            marker.remove();
            if (pothole != null){
                Double lat = marker.getPosition().latitude;
                Double lon = marker.getPosition().longitude;
                String author = pothole.getAuthor();
                deletePothole(lat,lon,author);

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


        // Hiển thị dialog

        btnCancel.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();

            // Đặt cửa sổ dialog chiếm toàn bộ màn hình
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.CENTER;

            // Áp dụng các thay đổi
            window.setAttributes(params);

            // Thiết lập background nếu cần
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

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

    private void showAddPotholeDialog(final float x, final float y, final float z) {
        if (isDialogShowing) {
            // Nếu hộp thoại đang hiển thị, không hiển thị thêm
            return;
        }

        isDialogShowing = true;

        new AlertDialog.Builder(getActivity())
                .setTitle("Add plothole")
                .setMessage("We detected a change in height. Would you like to add this pothole to the database?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Nếu người dùng đồng ý, thêm tọa độ vào cơ sở dữ liệu
                    getLastLocation();
                    Toast.makeText(getActivity(), "Latitude: " + latitude + ", Longitude: " + longitude, Toast.LENGTH_SHORT).show();
                    getAddressFromLocation(latitude, longitude);
                    int type = 1;
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

                })
                .setNegativeButton("No", null)
                .setOnDismissListener(dialog -> isDialogShowing = false)
                .show();
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

                        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {
                            mMap.setMyLocationEnabled(true);
                        }

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


                            if (distanceToNearest <= 10) {
                                markers.removeIf(marker -> {
                                    if (marker.getTag() == nearestPothole) {
                                        marker.remove(); // Xóa marker khỏi bản đồ
                                        return true; // Xóa khỏi danh sách markers
                                    }
                                    return false;
                                });
                                Toast.makeText(requireContext(), "Pothole passed!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            near_pothole.setText("No pothole");
                        }
                    }

                    if (isUserAtDestination(newLatLng, destination)) {
                        fusedLocationClient.removeLocationUpdates(this);
                        isRouteDrawn = false;
                        Toast.makeText(requireContext(), "You have arrived at your destination!", Toast.LENGTH_SHORT).show();
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
        return results[0] <= 10; // Người dùng được xem như đã đến đích nếu cách điểm đến < 20m
    }


    private void updateUserMarker(LatLng currentLatLng, float bearing) {
        if (mMap == null) {
            Toast.makeText(getActivity(), "Map chưa được khởi tạo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra tài nguyên
        BitmapDescriptor icon;
        try {
            icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_navigation);
        } catch (Exception e) {
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE); // Thay thế bằng mặc định
        }

        // Đảm bảo bearing hợp lệ
        float validBearing = (bearing >= 0 && bearing <= 360) ? bearing : 0;

        // Cập nhật hoặc thêm mới marker
        if (userMarker != null) {
            userMarker.setPosition(currentLatLng);
            userMarker.setRotation(validBearing);
        } else {
            userMarker = mMap.addMarker(new MarkerOptions()
                    .position(currentLatLng)
                    .icon(icon) // Icon xe
                    .title("Vị trí hiện tại")
                    .rotation(validBearing)
                    .anchor(0.5f, 0.5f)); // Căn giữa
        }
    }

    private String formatLatLng(LatLng latLng) {
        return latLng.longitude + "," + latLng.latitude;
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
                    // Lấy danh sách tọa độ
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
        for (LatLng routePoint : routePoints) {
            float[] results = new float[1];
            Location.distanceBetween(
                    potholeLocation.latitude, potholeLocation.longitude,
                    routePoint.latitude, routePoint.longitude,
                    results
            );

            // Kiểm tra khoảng cách (ví dụ: <= 50m)
            if (results[0] <= 50) {
                return true;
            }
        }
        return false;
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
                        // Thêm ổ gà thành công
                        Toast.makeText(getActivity(), "Pothole added successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Thêm ổ gà thất bại với thông báo lỗi từ server
                        Toast.makeText(getActivity(), apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Xử lý các lỗi với mã trạng thái
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
                // Lỗi mạng hoặc lỗi ngoài mong đợi
                Log.e("POTHOLE_ERROR", "Error adding pothole", t);
                Toast.makeText(getActivity(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePothole(PotholeClass pothole) {

        // Call Retrofit API to update pothole
        Call<PotholeResponse> call = apiService.updatePothole(pothole);
        call.enqueue(new Callback<PotholeResponse>() {
            @Override
            public void onResponse(Call<PotholeResponse> call, Response<PotholeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getActivity(), "Update successful", Toast.LENGTH_SHORT).show();
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

    private void deletePothole(Double lat, Double lon, String author) {

        Call<PotholeResponse> call = apiService.deletePothole(lat, lon, author);
        call.enqueue(new Callback<PotholeResponse>() {
            @Override
            public void onResponse(Call<PotholeResponse> call, Response<PotholeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getActivity(), "Delete successful", Toast.LENGTH_SHORT).show();
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


    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void cancelRoute() {
        if (currentPolyline != null) {
            currentPolyline.remove();  // Xóa đường dẫn trên bản đồ
            currentPolyline= null;  // Đặt lại biến currentRoute
            isRouteDrawn =false;
            mMap.setOnMapClickListener(null);
            destinationMarker2.remove();
            destinationMarker2 = null;
            destination = null;
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(false);
            }
            addMarkers();
            note.setVisibility(View.GONE);
        } else {
            Toast.makeText(requireContext(), "No route to cancel", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isEmulator() {
        return Build.FINGERPRINT.contains("generic") ||
                Build.MODEL.contains("Emulator") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                "google_sdk".equals(Build.PRODUCT);
    }
}
