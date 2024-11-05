package com.example.pothole;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button loginButton = findViewById(R.id.login);
        Button signupButton = findViewById(R.id.signup);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LogIn.class);
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
//        spinner.setSelection(0);
    }
}
