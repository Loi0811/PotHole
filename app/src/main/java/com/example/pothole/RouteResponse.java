package com.example.pothole;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import java.util.List;

public class RouteResponse {
    private List<Feature> features;

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public static class Feature {
        private Geometry geometry;

        public Geometry getGeometry() {
            return geometry;
        }

        public void setGeometry(Geometry geometry) {
            this.geometry = geometry;
        }
    }

    public static class Geometry {
        private List<double[]> coordinates;

        public List<double[]> getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(List<double[]> coordinates) {
            this.coordinates = coordinates;
        }
    }
}


