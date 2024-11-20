package com.example.pothole;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GeocodeResponse {
    @SerializedName("features")
    public List<Feature> features;

    public static class Feature {
        @SerializedName("geometry")
        public Geometry geometry;

        @SerializedName("properties")
        public Properties properties;

        public static class Geometry {
            @SerializedName("coordinates")
            public List<Double> coordinates;
        }

        public static class Properties {
            @SerializedName("label")
            public String label;
        }
    }
}

