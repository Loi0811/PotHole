package com.example.pothole;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class RouteRequest {
    @SerializedName("coordinates")
    public List<List<Double>> coordinates;

    public RouteRequest(LatLng start, LatLng end) {
        coordinates = new ArrayList<>();

        // Tạo danh sách tọa độ start và end
        List<Double> startCoord = new ArrayList<>();
        startCoord.add(start.longitude);
        startCoord.add(start.latitude);

        List<Double> endCoord = new ArrayList<>();
        endCoord.add(end.longitude);
        endCoord.add(end.latitude);

        coordinates.add(startCoord);
        coordinates.add(endCoord);
    }
}





