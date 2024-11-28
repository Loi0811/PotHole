package com.example.pothole;

import com.google.gson.annotations.SerializedName;

public class PotholeClass {

    @SerializedName("addressPothole")
    private AddressPotholeClass addressPothole;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    @SerializedName("date")
    private String date;

    @SerializedName("type")
    private Integer type;

    @SerializedName("author")
    private String author;

    public AddressPotholeClass getAddressPothole() {
        return addressPothole;
    }

    public void setAddressPothole(AddressPotholeClass addressPothole) {
        this.addressPothole = addressPothole;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
