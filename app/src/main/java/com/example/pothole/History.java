package com.example.pothole;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class History extends Fragment {

    public History() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        ListView listview = view.findViewById(R.id.list);
        ArrayList<HistoryItem> arrayList = new ArrayList<>();
        arrayList.add(new HistoryItem(R.drawable.pothole, "ADD REPORT", "Nguyen Du", "Di An", "Binh Duong", 10.88, 106.78, 10, 45, 2, 11, 2024, 3));
        arrayList.add(new HistoryItem(R.drawable.pothole, "ADD REPORT", "Nguyen Du", "Di An", "Binh Duong", 10.88, 106.78, 17, 45, 1, 11, 2024, 2));
        arrayList.add(new HistoryItem(R.drawable.pothole, "ADD REPORT", "Nguyen Du", "Di An", "Binh Duong", 10.88, 106.78, 10, 45, 1, 11, 2024, 3));
        arrayList.add(new HistoryItem(R.drawable.pothole, "UPDATE REPORT", "Nguyen Du", "Di An", "Binh Duong", 10.88, 106.78, 8, 45, 1, 11, 2024, 1));
        arrayList.add(new HistoryItem(R.drawable.pothole, "ADD REPORT", "Nguyen Du", "Di An", "Binh Duong", 10.88, 106.78, 10, 45, 29, 10, 2024, 3));
        arrayList.add(new HistoryItem(R.drawable.pothole, "ADD REPORT", "Nguyen Du", "Di An", "Binh Duong", 10.88, 106.78, 17, 45, 24, 10, 2024, 1));
        arrayList.add(new HistoryItem(R.drawable.pothole, "ADD REPORT", "Nguyen Du", "Di An", "Binh Duong", 10.88, 106.78, 10, 45, 20, 10, 2024, 3));
        arrayList.add(new HistoryItem(R.drawable.pothole, "UPDATE REPORT", "Nguyen Du", "Di An", "Binh Duong", 10.88, 106.78, 8, 45, 19, 10, 2024, 2));
        arrayList.add(new HistoryItem(R.drawable.pothole, "ADD REPORT", "Nguyen Du", "Di An", "Binh Duong", 10.88, 106.78, 10, 45, 2, 9, 2024, 1));
        arrayList.add(new HistoryItem(R.drawable.pothole, "ADD REPORT", "Nguyen Du", "Di An", "Binh Duong", 10.88, 106.78, 17, 45, 1, 9, 2024, 2));
        arrayList.add(new HistoryItem(R.drawable.pothole, "ADD REPORT", "Nguyen Du", "Di An", "Binh Duong", 10.88, 106.78, 10, 45, 1, 9, 2024, 3));
        arrayList.add(new HistoryItem(R.drawable.pothole, "UPDATE REPORT", "Nguyen Du", "Di An", "Binh Duong", 10.88, 106.78, 8, 45, 1, 9, 2024, 3));

        AdapterHistory adapter = new AdapterHistory(getActivity(), R.layout.item_history, arrayList);
        listview.setAdapter(adapter);

        return view;
    }
}
