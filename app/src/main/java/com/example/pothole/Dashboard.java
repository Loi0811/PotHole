package com.example.pothole;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class Dashboard extends Fragment {

    private Button weekButton, monthButton, yearButton;
    private TextView time;
    private ImageView pre, next, user;
    private BarChart barChart;
    private int time1 = 0;
    private int time2 = 0;

    // Sample data for each time frame
    private final float[] weekData = {5, 10, 7, 15, 8, 12, 9}; // Example data for week
    private final float[] monthData = {40, 34, 21, 10}; // Example data for month
    private final float[] yearData = {100, 150, 120, 180, 160, 200, 140, 200, 210, 195, 160, 220};

    private String[] labelw = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    private String[] labelm = {"W1", "W2", "W3", "W4"};
    private String[] labely = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};

    public Dashboard() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        PieChart pieChart = view.findViewById(R.id.dount_chart);
        user = view.findViewById(R.id.image);
        barChart = view.findViewById(R.id.bar_chart);
        weekButton = view.findViewById(R.id.week);
        monthButton = view.findViewById(R.id.month);
        yearButton = view.findViewById(R.id.year);
        TextView daily = view.findViewById(R.id.daily);

        // Set up user profile navigation
        user.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Profile.class);
            startActivity(intent);
        });

        // Pie chart setup
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(50));
        entries.add(new PieEntry(30));
        entries.add(new PieEntry(20));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(Color.parseColor("#E3B52A"), Color.parseColor("#DA7706"), Color.parseColor("#B90D0D"));
        dataSet.setSliceSpace(3f);
        dataSet.setValueTextColor(Color.WHITE); // Set value color
        dataSet.setValueTextSize(20f); // Set value size
        dataSet.setDrawValues(true); // Show values on slices

        // Format values to display as integers (no decimal places)
        dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value); // Convert to integer for display
            }
        });

        PieData data = new PieData(dataSet);

        // Configure the PieChart
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false); // Remove description
        pieChart.getLegend().setEnabled(false);      // Remove legend
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setCenterText("Total\n100");
        pieChart.setCenterTextSize(24f);
        pieChart.setCenterTextTypeface(Typeface.DEFAULT_BOLD);
        pieChart.animateY(1000);

        pieChart.invalidate(); // Refresh chart

        // Set up initial chart with week data
        setupBarChart(weekData, labelw);

        // Set click listeners for buttons
        weekButton.setOnClickListener(v -> {
            setupBarChart(weekData, labelw);
            weekButton.setBackgroundResource(R.drawable.button);
            monthButton.setBackgroundResource(R.drawable.button_off);
            yearButton.setBackgroundResource(R.drawable.button_off);
            daily.setText("Weekly Pot Hole");
            time1 = 0;
            time2 = 0;
        });

        monthButton.setOnClickListener(v -> {
            setupBarChart(monthData, labelm);
            weekButton.setBackgroundResource(R.drawable.button_off);
            monthButton.setBackgroundResource(R.drawable.button);
            yearButton.setBackgroundResource(R.drawable.button_off);
            daily.setText("Monthly Pot Hole");
            time1 = 1;
            time2 = 0;
        });

        yearButton.setOnClickListener(v -> {
            setupBarChart(yearData, labely);
            weekButton.setBackgroundResource(R.drawable.button_off);
            monthButton.setBackgroundResource(R.drawable.button_off);
            yearButton.setBackgroundResource(R.drawable.button);
            daily.setText("Yearly Pot Hole");
            time1 = 2;
            time2 = 0;
        });

        return view;
    }

    private void setupBarChart(float[] data, String[] labels) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            entries.add(new BarEntry(i, data[i]));
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(14f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // Remove legend
        barChart.getLegend().setEnabled(false);

        // Customize X-axis
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setTextSize(14f);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setLabelRotationAngle(0f);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setLabelCount(labels.length);
        barChart.getXAxis().setTypeface(Typeface.DEFAULT_BOLD);

        barChart.getXAxis().setDrawGridLines(false); // Remove X-axis grid lines
        barChart.getAxisLeft().setDrawGridLines(false); // Remove left Y-axis grid lines
        barChart.getAxisRight().setDrawGridLines(false); // Remove right Y-axis grid lines

        // Remove Y-axis on the right
        barChart.getAxisRight().setEnabled(false);

        // Refresh the chart
        barChart.invalidate();
    }
}
