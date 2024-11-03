package com.example.pothole;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class AdapterHistory extends BaseAdapter {
    private List<HistoryItem> listFood;
    private Context context;
    private int layout;

    public AdapterHistory(Context context, int layout, List<HistoryItem> listFood) {
        this.context = context;
        this.layout = layout;
        this.listFood = listFood;
    }

    @Override
    public int getCount() {
        return listFood.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(layout,null);
        HistoryItem historyItem = listFood.get(position);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) layoutParams).setMargins(10, 5, 10, 5); // left, top, right, bottom
            view.setLayoutParams(layoutParams);
        }

        ImageView image = view.findViewById(R.id.image);
        TextView action = view.findViewById(R.id.action);
        TextView adress = view.findViewById(R.id.adress);
        TextView y = view.findViewById(R.id.y);
        TextView x = view.findViewById(R.id.x);
        TextView time = view.findViewById(R.id.time);
        View type = view.findViewById(R.id.type_color);

        String street = historyItem.getStreet();
        String adress1 = historyItem.getAdress1();
        String adress2 = historyItem.getAdress1();

        int h = historyItem.getH();
        int m = historyItem.getM();
        int day = historyItem.getDay();
        int month = historyItem.getMonth();
        int year = historyItem.getYear();

        double y_value = historyItem.getY();
        double x_value = historyItem.getX();

        int type_color = historyItem.getType();

        image.setImageResource(historyItem.getImage());
        action.setText(historyItem.getAction());
        adress.setText(street + "st,\n" + adress1 + "," + adress2);
        y.setText(String.valueOf(y_value));
        x.setText(String.valueOf(x_value));
        time.setText(h + ":" + m + "-" + day + "/" + month + "/" + year);
        switch (type_color){
            case 1:
                type.setBackgroundResource(R.drawable.caution_type);
                view.setBackgroundResource(R.drawable.caution);
                break;
            case 2:
                type.setBackgroundResource(R.drawable.warning_type);
                view.setBackgroundResource(R.drawable.warning);
                break;
            case 3:
                type.setBackgroundResource(R.drawable.danger_type);
                view.setBackgroundResource(R.drawable.danger);
                break;
        }
        return view;
    }
}
