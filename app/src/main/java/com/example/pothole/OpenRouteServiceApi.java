package com.example.pothole;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface OpenRouteServiceApi {
    @GET("geocode/search")
    Call<GeocodeResponse> searchLocation(
            @Query("api_key") String apiKey,
            @Query("text") String query,
            @Query("size") int maxResults
    );

    @GET("v2/directions/driving-car")
    Call<RouteResponse> getRoute(
            @Query("api_key") String apiKey,
            @Query("start") String start,
            @Query("end") String end
    );

}
