package com.example.pothole;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import java.util.List;

public interface NominatimApi {
    @GET("search")
    Call<List<LocationResponse>> searchLocation(
            @Query("q") String query,
            @Query("format") String format,
            @Query("addressdetails") int addressDetails,
            @Query("limit") int limit
    );

}

