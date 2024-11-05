package com.example.pothole;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomSpinnerAdapter extends ArrayAdapter<SpinnerItem> {
    private final Context context;
    private final SpinnerItem[] items;

    public CustomSpinnerAdapter(Context context, SpinnerItem[] items) {
        super(context, R.layout.spinner_item, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent);
    }

    private View createViewFromResource(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.spinner_item, parent, false);
        }

        ImageView imageView = view.findViewById(R.id.icon);
        TextView textView = view.findViewById(R.id.text);

        SpinnerItem item = items[position];
        imageView.setImageResource(item.getImageResId());
        textView.setText(item.getText());

        return view;
    }
}