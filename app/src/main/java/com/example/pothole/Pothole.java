package com.example.pothole;

public class Pothole {
    private AddressPothole addressPothole;
    private Double latitude;
    private Double longitude;
    private String date;
    private Integer type;
    private String author;

    public AddressPothole getAddressPothole() {
        return addressPothole;
    }

    public void setAddressPothole(AddressPothole addressPothole) {
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
