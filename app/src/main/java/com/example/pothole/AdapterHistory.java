package com.example.pothole;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class AdapterHistory extends BaseAdapter {
    private List<HistoryClass> listFood;
    private Context context;
    private int layout;

    public AdapterHistory(Context context, int layout, List<HistoryClass> listFood) {
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
        HistoryClass historyClass = listFood.get(position);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) layoutParams).setMargins(10, 5, 10, 5); // left, top, right, bottom
            view.setLayoutParams(layoutParams);
        }

        ImageView image = view.findViewById(R.id.image);
        TextView action = view.findViewById(R.id.action);
        TextView address1 = view.findViewById(R.id.address1);
        TextView address2 = view.findViewById(R.id.address2);
        TextView y = view.findViewById(R.id.y);
        TextView x = view.findViewById(R.id.x);
        TextView time = view.findViewById(R.id.time);
        View type = view.findViewById(R.id.type_color);
        TextView point_action = view.findViewById(R.id.point);

        AddressPotholeClass addressPothole = historyClass.getAddressPothole();


        String street = addressPothole.getStreetName();
        String district = addressPothole.getDistrict();
        String province = addressPothole.getProvince();

        String date = historyClass.getDate();

        double y_value = historyClass.getLatitude();
        double x_value = historyClass.getLongitude();

        int type_color = historyClass.getType();
        int point = historyClass.getPoint();

        action.setText(historyClass.getAction());
        address1.setText(street);
        address2.setText(district + "," + province);
        y.setText(String.valueOf(y_value));
        x.setText(String.valueOf(x_value));
        time.setText(date);
        point_action.setText(String.valueOf(point));

        if (historyClass.getAction().equals("ADD POTHOLE")){
            image.setImageResource(R.drawable.plus);
        } else if (historyClass.getAction().equals("UPDATE POTHOLE(ADDRESS)")||historyClass.getAction().equals("UPDATE POTHOLE(TYPE)")||historyClass.getAction().equals("UPDATE POTHOLE(ADDRESS,TYPE)")){
            image.setImageResource(R.drawable.edit);
        } else {
            image.setImageResource(R.drawable.trash);
        }

        if (point > 0){
            String plus = "+" + point;
            point_action.setText(plus);
        } else if (point < 0) {
            point_action.setText(String.valueOf(point));
        } else {
            String plus = "+" + point;
            point_action.setText(plus);
        }

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
