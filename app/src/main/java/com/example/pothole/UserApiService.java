package com.example.pothole;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserApiService {
    @POST("register")
    Call<ApiResponse> registerUser(@Body User user);

    @PUT("update/{email}")
    Call<ApiResponse> updateUser(
            @Path("email") String email,
            @Body Userupdate user
    );

    @POST("login")
    Call<ApiResponse> loginUser(@Body Userlogin user);

    @PUT("update-password")
    Call<ApiResponse> updatePassword(@Body PasswordUpdateRequest request);

    @POST("add-pothole")
    Call<ApiResponse> addPothole(@Body Pothole pothole);

    @GET("/get-potholes")
    Call<PotholeResponse> getPotholes(@Query("author") String author);

    @PUT("update-pothole")
    Call<PotholeResponse> updatePothole(@Body PotholeClass pothole);

    @DELETE("delete-pothole")
    Call<PotholeResponse> deletePothole(@Query("latitude") double latitude, @Query("longitude") double longitude, @Query("author") String author);
}

