package com.example.pothole;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.ArrayList;
import java.util.List;

public class Map extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.btn_map);

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.btn_home) {
                    startActivity(new Intent(Map.this, Home.class));
                    return true;
                } else if (itemId == R.id.btn_history) {
                    startActivity(new Intent(Map.this, History.class));
                    return true;
                } else if (itemId == R.id.btn_setting) {
                    startActivity(new Intent(Map.this, Profile.class));
                    return true;
                }
                return false;
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ImageView jumpToMyLocationButton = findViewById(R.id.my_location);
        jumpToMyLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();
            }
        });
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permissions if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                    } else {
                        Toast.makeText(Map.this, "Unable to find current location.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addMarkers() {
        // Example markers list
        List<MarkerData> markers = new ArrayList<>();
        markers.add(new MarkerData(new LatLng(10.87, 106.8), 1, "Marker 1"));
        markers.add(new MarkerData(new LatLng(10.88, 106.78), 2, "Marker 2"));
        markers.add(new MarkerData(new LatLng(10.871, 106.8), 2, "Marker 3"));
        markers.add(new MarkerData(new LatLng(10.87, 106.803), 3, "Marker 4"));
        markers.add(new MarkerData(new LatLng(10.88, 106.8), 3, "Marker 5"));

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
                Toast.makeText(this, "Permission denied to access location.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
