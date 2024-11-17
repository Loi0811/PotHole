package com.example.pothole;

public class Userupdate {
    private String name;
    private String phone;
    private AddressUser address;
    private String birthday;
    private String gender;

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public AddressUser getAddress() {
        return address;
    }

    public void setAddress(AddressUser addressUser) {
        this.address = addressUser;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
