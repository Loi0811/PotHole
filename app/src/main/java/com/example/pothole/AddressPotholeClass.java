package com.example.pothole;

import com.google.gson.annotations.SerializedName;

public class AddressPotholeClass {

    @SerializedName("streetName")
    private String streetName;

    @SerializedName("district")
    private String district;

    @SerializedName("province")
    private String province;

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }
}
