package com.example.pothole;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {


    private ImageView googlebtn;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    private UserApiService apiService;
    private Boolean checkUser;
    private String emailuser,name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiService = ApiClient.getClient(isEmulator()).create(UserApiService.class);

        Button loginButton = findViewById(R.id.login);
        Button signupButton = findViewById(R.id.signup);
        googlebtn = findViewById(R.id.google);

        checkUser = false;

        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String language = sharedPreferences.getString("language","English");

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this,gso);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                String email = sharedPreferences.getString("Email",null);
                Intent intent;
                if (email != null){
                    intent = new Intent(MainActivity.this, Home.class);
                } else {
                    intent = new Intent(MainActivity.this, LogIn.class);
                }
                startActivity(intent);
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignUp.class);
                startActivity(intent);
            }
        });

        googlebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
                if(acct!=null){
                    nextActivity();
                } else{
                    signInGoogle();
                }
            }
        });

        Spinner spinner = findViewById(R.id.spinner);

//        // Create an array of spinner items
        SpinnerItem[] items = {
                new SpinnerItem(R.drawable.uk, "English(UK)"),
                new SpinnerItem(R.drawable.vietnam, "Vietnamese")
        };
//
//        // Set the adapter
        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(this, items);
        spinner.setAdapter(adapter);
//
        if (language.equals("English")){
            spinner.setSelection(0);
        } else if (language.equals("Vietnamese")){
            spinner.setSelection(1);
        }

    }

    void signInGoogle(){
        Intent signInIntent = gsc.getSignInIntent();
        startActivityForResult(signInIntent,1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                task.getResult(ApiException.class);
                nextActivity();
            } catch (ApiException e) {
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }

    }
    void nextActivity(){
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if(acct!=null){
            name = acct.getDisplayName();
            emailuser = acct.getEmail();
            emailuser = "G-" + emailuser;
        }
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Email", emailuser);
        editor.putString("Password", "0");
        editor.putBoolean("GoogleLogin", true);
        editor.apply();
        searchUser(emailuser,name);

    }

    private void searchUser(String email, String nameuser) {
        apiService.searchUser(email).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    checkUser = response.body().isStatus();
                    if (checkUser) {
                        Toast.makeText(MainActivity.this, "User exists", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this,Home.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();
                        registerUser(nameuser,email);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Failed to get response", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUser(String username, String useremail) {


        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        String currentDate = sdf.format(new Date());

        AddressUser address = new AddressUser();
        address.setDistrict("district");
        address.setProvince("province");

        User user = new User();
        user.setName(username);
        user.setEmail(useremail);
        user.setPassword("0");
        user.setPhone("phone");
        user.setAddress(address);
        user.setBirthday("2/2/2000");
        user.setGender("gender");
        user.setCreate(currentDate);
        user.setAvatar("id0");
        user.setTravel(0.0);

        apiService.registerUser(user).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse != null && apiResponse.isStatus()) {
                        Toast.makeText(MainActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this,Information.class);
                        intent.putExtra("sendemail", emailuser);
                        intent.putExtra("sendname", name);
                        intent.putExtra("google", true);
                        startActivity(intent);
                    } else {
                            Toast.makeText(MainActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Check if the response code is 400 (Bad Request)
                    if (response.code() == 400 && response.errorBody() != null) {
                        try {
                            // Parse the error body to get the specific message
                            String errorBody = response.errorBody().string();
                            JSONObject jsonObject = new JSONObject(errorBody);
                            String errorMessage = jsonObject.optString("message", "Registration failed");

                            // Show only the error message in the Toast
                            Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Registration error", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }
            }


            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("API_ERROR", "Network error", t);
                Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static boolean isEmulator() {
        return Build.FINGERPRINT.contains("generic") ||
                Build.MODEL.contains("Emulator") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                "google_sdk".equals(Build.PRODUCT);
    }
}
