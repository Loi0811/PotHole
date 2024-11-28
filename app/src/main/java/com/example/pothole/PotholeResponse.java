package com.example.pothole;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PotholeResponse {

    @SerializedName("status")
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<PotholeClass> potholes;

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<PotholeClass> getPotholes() {
        return potholes;
    }
}
