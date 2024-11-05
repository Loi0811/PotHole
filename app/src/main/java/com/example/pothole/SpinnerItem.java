package com.example.pothole;

public class SpinnerItem {
    private final int imageResId;
    private final String text;

    public SpinnerItem(int imageResId, String text) {
        this.imageResId = imageResId;
        this.text = text;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getText() {
        return text;
    }
}

