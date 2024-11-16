package com.example.pothole;

public class ApiResponseLogin {
    private boolean status;
    private String message;
    private String token;

    // Getters and setters
    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
