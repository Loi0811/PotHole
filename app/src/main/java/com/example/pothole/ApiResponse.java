package com.example.pothole;

import com.google.gson.annotations.SerializedName;

public class ApiResponse {
    private boolean status;
    private String success;
    private String token;

    @SerializedName("data")
    private User data;

    // Getters and Setters

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return success;
    }

    public void setMessage(String success) {
        this.success = success;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public User getData() {
        return data;
    }

    public void setData(User data) {
        this.data = data;
    }
}

