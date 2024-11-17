package com.example.pothole;

public class User {
    private String name;
    private String email;
    private String password;
    private String phone;
    private AddressUser address;
    private String birthday;
    private String gender;
    private String cteate;

    // Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public AddressUser getAddress() {
        return address;
    }

    public void setAddress(AddressUser addressUser) {
        this.address = addressUser;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCteate() {
        return cteate;
    }

    public void setCteate(String cteate) {
        this.cteate = cteate;
    }
}

