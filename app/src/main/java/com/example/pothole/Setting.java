package com.example.pothole;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Setting extends Fragment {

    public Setting() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        // Setup logout item
        setupItem(view, R.id.logOutItem, MainActivity.class);


        return view;
    }

    private void setupItem(View view, int itemId, Class<?> activityClass) {
        LinearLayout item = view.findViewById(itemId);
        item.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), activityClass);
            if (itemId == R.id.logOutItem) intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
    }
}
