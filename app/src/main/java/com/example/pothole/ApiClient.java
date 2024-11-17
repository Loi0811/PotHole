package com.example.pothole;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient(boolean isEmulator) {
        String baseUrl;

        if (isEmulator) {
            // URL for Emulator
            baseUrl = "http://10.0.2.2:3000/";
        } else {
            // URL for Real Device - replace with your local IP
            baseUrl = "http://10.0.223.56:3000/";
        }

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}


