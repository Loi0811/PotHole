package com.example.pothole;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import retrofit2.Call;

import retrofit2.Callback;
import retrofit2.Response;

public class Map extends Fragment implements OnMapReadyCallback {

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
    private ImageView clearButton, runButton;
    private LatLng destination;
    private Polyline currentPolyline;
    private LocationCallback locationCallback;
    private UserApiService apiService;
    private double latitude, longitude;
    private String streetName,district, province;


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

        searchLocation = view.findViewById(R.id.search);
        clearButton = view.findViewById(R.id.clear);
        runButton = view.findViewById(R.id.run);

        clearButton.setOnClickListener(v -> {
            searchLocation.setText("");
            clearButton.setVisibility(View.GONE);
        });

        runButton.setOnClickListener(v -> {
            if (destination != null) {
                drawRouteToDestination(destination);
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

        // Add markers
        addMarkers();

        // Get current location and move camera to it
        getCurrentLocation();
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

        // Đặt múi giờ là UTC (Z ở cuối chỉ UTC)
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Lấy thời gian hiện tại
        Date date = new Date();

        // Định dạng thời gian hiện tại và trả về kết quả
        return dateFormat.format(date);
    }

    private void addMarkers() {
        // Example markers list
        List<MarkerData> markers = new ArrayList<>();
        markers.add(new MarkerData(new LatLng(10.87, 106.8), 1, "Caution"));
        markers.add(new MarkerData(new LatLng(10.88, 106.78), 2, "Warning"));
        markers.add(new MarkerData(new LatLng(10.871, 106.8), 2, "Warning"));
        markers.add(new MarkerData(new LatLng(10.87, 106.803), 3, "Danger"));
        markers.add(new MarkerData(new LatLng(10.88, 106.8), 3, "Danger"));

        // Add markers to map
        for (MarkerData marker : markers) {
            mMap.addMarker(new MarkerOptions()
                    .position(marker.getLatLng())
                    .title(marker.getTitle())
                    .icon(getMarkerColor(marker.getType())));
        }
    }

    private BitmapDescriptor getMarkerColor(int type) {
        switch (type) {
            case 1:
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
            case 2:
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
            case 3:
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            default:
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        }
    }

    private static class MarkerData {
        private final LatLng latLng;
        private final int type;
        private final String title;

        public MarkerData(LatLng latLng, int type, String title) {
            this.latLng = latLng;
            this.type = type;
            this.title = title;
        }

        public LatLng getLatLng() {
            return latLng;
        }

        public int getType() {
            return type;
        }

        public String getTitle() {
            return title;
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
                    String author = "author@gm.com";
                    AddressPothole addressPothole = new AddressPothole();
                    addressPothole.setStreetName(streetName);
                    addressPothole.setDistrict(district);
                    addressPothole.setProvince(province);

                    Pothole pothole = new Pothole();
                    pothole.setAddressPothole(addressPothole);
                    pothole.setLatitude(latitude);
                    pothole.setLongitude(longitude);
                    pothole.setDate(getCurrentTime());
                    pothole.setType(type);
                    pothole.setAuthor(author);
                    addPothole(pothole);

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

        NominatimApi api = RetrofitClient.getInstance().create(NominatimApi.class);
        Call<List<LocationResponse>> call = api.searchLocation(query, "json", 0, 5);

        call.enqueue(new retrofit2.Callback<List<LocationResponse>>() {
            @Override
            public void onResponse(Call<List<LocationResponse>> call, retrofit2.Response<List<LocationResponse>> response) {
                Log.d("NominatimResponse", "Response: " + response.body());
                if (response.isSuccessful() && response.body() != null) {
                    List<LocationResponse> locations = response.body();
                    List<String> suggestions = new ArrayList<>();
                    for (LocationResponse location : locations) {
                        suggestions.add(location.getDisplayName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            getActivity(),
                            android.R.layout.simple_dropdown_item_1line,
                            suggestions
                    );
                    searchBox.setAdapter(adapter);
                    searchBox.showDropDown();

                    // Nhấn vào gợi ý để di chuyển camera
                    searchBox.setOnItemClickListener((parent, view, position, id) -> {
                        LocationResponse selectedLocation = locations.get(position);
                        destination = new LatLng(selectedLocation.getLat(), selectedLocation.getLon());


                        mMap.addMarker(new MarkerOptions()
                                .position(destination)
                                .title(selectedLocation.getDisplayName()));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destination, 17));
                    });

                } else {
                    Toast.makeText(getActivity(), "No suggestions found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<LocationResponse>> call, Throwable t) {// Ẩn progress bar
                Toast.makeText(getActivity(), "Failed to fetch suggestions", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void drawRouteToDestination(LatLng destination) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
            return;
        }

        // Bật cập nhật vị trí liên tục
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                // Lấy vị trí hiện tại
                Location currentLocation = locationResult.getLastLocation();
                LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                // Cập nhật Polyline
                updatePolyline(currentLatLng, destination);

                // Di chuyển camera đến vị trí hiện tại
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));
            }
        };

        fusedLocationClient.requestLocationUpdates(
                LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(5000),  // Cập nhật mỗi 5 giây
                locationCallback,
                null
        );
    }

    private void updatePolyline(LatLng start, LatLng end) {
        if (currentPolyline != null) {
            currentPolyline.remove(); // Xóa đường cũ nếu tồn tại
        }

        // Tạo đường mới
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(start)
                .add(end)
                .width(10)
                .color(Color.BLUE)
                .geodesic(true);

        currentPolyline = mMap.addPolyline(polylineOptions);
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

    public static boolean isEmulator() {
        return Build.FINGERPRINT.contains("generic") ||
                Build.MODEL.contains("Emulator") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                "google_sdk".equals(Build.PRODUCT);
    }
}
