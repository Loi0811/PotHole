package com.example.pothole;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class History extends Fragment {

    private ArrayList<HistoryClass> histories = new ArrayList<>();
    private ArrayList<HistoryClass> list_histories = new ArrayList<>();
    private UserApiService apiService;
    private String userEmail;
    private ListView listview;
    private AdapterHistory adapter;

    public History() {
        // Required empty public constructor
    }

    public void addItemHistory(HistoryClass historyClass){
        list_histories.add(0,historyClass);

        adapter.notifyDataSetChanged();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        apiService = ApiClient.getClient(isEmulator()).create(UserApiService.class);

        Home homeActivity = (Home) getActivity();
        if (homeActivity != null) {
            userEmail = homeActivity.getUseremail();
        }
        listview = view.findViewById(R.id.list);

        fetchHistoriesByAuthor(userEmail);

        return view;
    }

    private void fetchHistoriesByAuthor(String authorEmail) {
        apiService.getHistories(authorEmail).enqueue(new Callback<HistoryResponse>() {
            @Override
            public void onResponse(Call<HistoryResponse> call, Response<HistoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HistoryResponse historyResponse = response.body();
                    histories = historyResponse.getHistories();
                    if (histories != null) {
                        list_histories = histories;
                        adapter = new AdapterHistory(getActivity(), R.layout.item_history, list_histories);
                        listview.setAdapter(adapter);
                    } else {
                        Toast.makeText(requireActivity(), "Histories list is empty", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireActivity(), "Failed to load histories", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HistoryResponse> call, Throwable t) {
                Toast.makeText(requireActivity(), "Error fetching histories: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
