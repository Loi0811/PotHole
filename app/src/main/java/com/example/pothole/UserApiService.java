package com.example.pothole;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

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
}

