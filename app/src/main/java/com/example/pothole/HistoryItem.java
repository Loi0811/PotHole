package com.example.pothole;


public class HistoryItem {
    private int image;
    private String action;
    private String street;
    private String adress1;
    private String adress2;
    private double y;
    private double x;
    private int h;
    private int m;
    private int day;
    private int month;
    private int year;
    private int type;

    public HistoryItem(int image, String action,String street, String adress1, String adress2, double y, double x, int h, int m, int day, int month, int year, int type){
        this.image = image;
        this.action = action;
        this.street = street;
        this.adress1 = adress1;
        this.adress2 = adress2;
        this.y = y;
        this.x = x;
        this.h = h;
        this.m = m;
        this.day = day;
        this.month = month;
        this.year = year;
        this.type = type;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getAdress1() {
        return adress1;
    }

    public void setAdress1(String adress1) {
        this.adress1 = adress1;
    }

    public String getAdress2() {
        return adress2;
    }

    public void setAdress2(String adress2) {
        this.adress2 = adress2;
    }

    public double getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public int getM() {
        return m;
    }

    public void setM(int m) {
        this.m = m;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
