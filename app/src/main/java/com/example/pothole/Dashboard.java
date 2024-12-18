package com.example.pothole;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import java.util.Calendar;
import java.util.TimeZone;

public class Dashboard extends Fragment{

    private Button weekButton, monthButton, yearButton;
    private TextView time,name, dayscreate;
    private ImageView pre, next, user_profile;
    private PieChart pieChart;
    private BarChart barChart;
    private int typeTime = 0;
    private int timeAgo = 0;

    // Sample data for each time frame
    private final int[] weekData1 = new int[7];
    private final int[] monthData1 = new int[6];
    private final int[] weekData2 = new int[7];
    private final int[] monthData2 = new int[6];
    private final int[] weekData3 = new int[7];
    private final int[] monthData3 = new int[6];
    private final int[] yearData1 = new int[12];

    private String[] labelw = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    private String[] labelm = {"W1", "W2", "W3", "W4", "W5", "W6"};
    private String[] labely = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};

    public User user;
    private String email;
    private UserApiService apiService;
    private List<PotholeClass> potholes_user = new ArrayList<>();
    private Integer danger = 0, warning = 0 , caution = 0;

    private long days;

    private Integer typeBarChart;

    public Dashboard() {

    }

    public void ChartAfterAdd(PotholeClass potholeClass){
        Log.d("DASHBOARD","OK chart");
        switch (potholeClass.getType()) {
            case 1:
                caution++;
                break;
            case 2:
                warning++;
                break;
            case 3:
                danger++;
                break;
            default:
                // Không xử lý type khác
                break;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmX", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar calendarWeek1 = Calendar.getInstance();
        calendarWeek1.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendarWeek1.setFirstDayOfWeek(Calendar.MONDAY); // Bắt đầu tuần từ thứ Hai

        // Xác định đầu tuần và cuối tuần của tuần này
        calendarWeek1.add(Calendar.WEEK_OF_YEAR, 0);
        calendarWeek1.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // Đầu tuần
        calendarWeek1.set(Calendar.HOUR_OF_DAY, 0);
        calendarWeek1.set(Calendar.MINUTE, 0);
        calendarWeek1.set(Calendar.SECOND, 0);
        calendarWeek1.set(Calendar.MILLISECOND, 0);
        Date startOfWeek1 = calendarWeek1.getTime();

        calendarWeek1.add(Calendar.DAY_OF_WEEK, 6); // Cuối tuần
        calendarWeek1.add(Calendar.DAY_OF_WEEK, 6);
        calendarWeek1.set(Calendar.HOUR_OF_DAY, 23);
        calendarWeek1.set(Calendar.MINUTE, 59);
        calendarWeek1.set(Calendar.SECOND, 59);
        calendarWeek1.set(Calendar.MILLISECOND, 999);
        Date endOfWeek1 = calendarWeek1.getTime();

        Calendar calendarMonth1 = Calendar.getInstance();
        calendarMonth1.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendarMonth1.setFirstDayOfWeek(Calendar.MONDAY); // Bắt đầu tuần từ thứ Hai

        // Xác định đầu tháng và cuối tháng của tháng này
        calendarMonth1.add(Calendar.MONTH, 0);
        calendarMonth1.set(Calendar.DAY_OF_MONTH, 1); // Đầu tháng
        calendarMonth1.set(Calendar.HOUR_OF_DAY, 0);
        calendarMonth1.set(Calendar.MINUTE, 0);
        calendarMonth1.set(Calendar.SECOND, 0);
        calendarMonth1.set(Calendar.MILLISECOND, 0);
        Date startOfMonth1 = calendarMonth1.getTime();

        calendarMonth1.set(Calendar.DAY_OF_MONTH, calendarMonth1.getActualMaximum(Calendar.DAY_OF_MONTH)); // Cuối tháng
        calendarMonth1.set(Calendar.HOUR_OF_DAY, 23);
        calendarMonth1.set(Calendar.MINUTE, 59);
        calendarMonth1.set(Calendar.SECOND, 59);
        calendarMonth1.set(Calendar.MILLISECOND, 999);
        Date endOfMonth1 = calendarMonth1.getTime();

        Calendar calendarYear1 = Calendar.getInstance();
        calendarYear1.setTimeZone(TimeZone.getTimeZone("UTC"));

        Log.d("DATE_POTHOLE", "date:" + potholeClass.getDate());

        try {
            Date potholeDate = sdf.parse(potholeClass.getDate());
            Log.d("DATE","date" + potholeDate);
            if (potholeDate != null) {
                // Theo tuần
                if (!potholeDate.before(startOfWeek1) && !potholeDate.after(endOfWeek1)) {
                    calendarWeek1.setTime(potholeDate);
                    int dayOfWeekIndex = calendarWeek1.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
                    if (dayOfWeekIndex < 0)
                        dayOfWeekIndex = 6; // Chuyển Chủ Nhật thành cuối tuần (index 6)
                    weekData1[dayOfWeekIndex]++;
                    Log.d("WEEK","Da Tang");
                }

                // Theo tháng
                if (!potholeDate.before(startOfMonth1) && !potholeDate.after(endOfMonth1)) {
                    calendarMonth1.setTime(potholeDate);
                    int weekOfMonthIndex = calendarMonth1.get(Calendar.WEEK_OF_MONTH) - 1;
                    monthData1[weekOfMonthIndex]++;
                    Log.d("MONTH","Da Tang");
                }

                calendarYear1.setTime(potholeDate);
                int monthIndex = calendarYear1.get(Calendar.MONTH); // January=0
                yearData1[monthIndex]++;
                Log.d("YEAR","Da Tang");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        setupPieChart(danger, warning, caution);
        switch (typeTime) {
            case 0:
                setupBarChart(weekData1,labelw);
                time.setText("This week");
                timeAgo = 0;
                break;
            case 1:
                setupBarChart(monthData1,labelm);
                time.setText("This month");
                timeAgo = 0;
                break;
            case 2:
                setupBarChart(yearData1,labely);
                time.setText("This year");
                timeAgo = 0;
                break;
        }
    }

    public void ChartAfterUpdate(Integer old_type, Integer new_type){
        switch (new_type) {
            case 1:
                caution++;
                break;
            case 2:
                warning++;
                break;
            case 3:
                danger++;
                break;
            default:
                // Không xử lý type khác
                break;
        }
        switch (old_type) {
            case 1:
                caution--;
                break;
            case 2:
                warning--;
                break;
            case 3:
                danger--;
                break;
            default:
                // Không xử lý type khác
                break;
        }
        setupPieChart(danger, warning, caution);
    }

    public void ChartAfterDelete(Integer type, String date){
        switch (type) {
            case 1:
                caution--;
                break;
            case 2:
                warning--;
                break;
            case 3:
                danger--;
                break;
            default:
                // Không xử lý type khác
                break;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar calendarWeek1 = Calendar.getInstance();
        calendarWeek1.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendarWeek1.setFirstDayOfWeek(Calendar.MONDAY); // Bắt đầu tuần từ thứ Hai

        // Xác định đầu tuần và cuối tuần của tuần này
        calendarWeek1.add(Calendar.WEEK_OF_YEAR, 0);
        calendarWeek1.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // Đầu tuần
        calendarWeek1.set(Calendar.HOUR_OF_DAY, 0);
        calendarWeek1.set(Calendar.MINUTE, 0);
        calendarWeek1.set(Calendar.SECOND, 0);
        calendarWeek1.set(Calendar.MILLISECOND, 0);
        Date startOfWeek1 = calendarWeek1.getTime();

        calendarWeek1.add(Calendar.DAY_OF_WEEK, 6); // Cuối tuần
        calendarWeek1.add(Calendar.DAY_OF_WEEK, 6);
        calendarWeek1.set(Calendar.HOUR_OF_DAY, 23);
        calendarWeek1.set(Calendar.MINUTE, 59);
        calendarWeek1.set(Calendar.SECOND, 59);
        calendarWeek1.set(Calendar.MILLISECOND, 999);
        Date endOfWeek1 = calendarWeek1.getTime();

        Calendar calendarMonth1 = Calendar.getInstance();
        calendarMonth1.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendarMonth1.setFirstDayOfWeek(Calendar.MONDAY); // Bắt đầu tuần từ thứ Hai

        // Xác định đầu tháng và cuối tháng của tháng này
        calendarMonth1.add(Calendar.MONTH, 0);
        calendarMonth1.set(Calendar.DAY_OF_MONTH, 1); // Đầu tháng
        calendarMonth1.set(Calendar.HOUR_OF_DAY, 0);
        calendarMonth1.set(Calendar.MINUTE, 0);
        calendarMonth1.set(Calendar.SECOND, 0);
        calendarMonth1.set(Calendar.MILLISECOND, 0);
        Date startOfMonth1 = calendarMonth1.getTime();

        calendarMonth1.set(Calendar.DAY_OF_MONTH, calendarMonth1.getActualMaximum(Calendar.DAY_OF_MONTH)); // Cuối tháng
        calendarMonth1.set(Calendar.HOUR_OF_DAY, 23);
        calendarMonth1.set(Calendar.MINUTE, 59);
        calendarMonth1.set(Calendar.SECOND, 59);
        calendarMonth1.set(Calendar.MILLISECOND, 999);
        Date endOfMonth1 = calendarMonth1.getTime();

        Calendar calendarWeek2 = Calendar.getInstance();
        calendarWeek2.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendarWeek2.setFirstDayOfWeek(Calendar.MONDAY); // Bắt đầu tuần từ thứ Hai

        // Xác định đầu tuần và cuối tuần cua 1 tuan truoc
        calendarWeek2.add(Calendar.WEEK_OF_YEAR, -1);
        calendarWeek2.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // Đầu tuần
        calendarWeek2.set(Calendar.HOUR_OF_DAY, 0);
        calendarWeek2.set(Calendar.MINUTE, 0);
        calendarWeek2.set(Calendar.SECOND, 0);
        calendarWeek2.set(Calendar.MILLISECOND, 0);
        Date startOfWeek2 = calendarWeek2.getTime();

        calendarWeek2.add(Calendar.DAY_OF_WEEK, 6); // Cuối tuần
        calendarWeek2.set(Calendar.HOUR_OF_DAY, 23);
        calendarWeek2.set(Calendar.MINUTE, 59);
        calendarWeek2.set(Calendar.SECOND, 59);
        calendarWeek2.set(Calendar.MILLISECOND, 999);
        Date endOfWeek2 = calendarWeek2.getTime();

        Calendar calendarMonth2 = Calendar.getInstance();
        calendarMonth2.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendarMonth2.setFirstDayOfWeek(Calendar.MONDAY); // Bắt đầu tuần từ thứ Hai

        // Xác định đầu tháng và cuối tháng cua 1 thang truoc
        calendarMonth2.add(Calendar.MONTH, -1);
        calendarMonth2.set(Calendar.DAY_OF_MONTH, 1); // Đầu tháng
        calendarMonth2.set(Calendar.HOUR_OF_DAY, 0);
        calendarMonth2.set(Calendar.MINUTE, 0);
        calendarMonth2.set(Calendar.SECOND, 0);
        calendarMonth2.set(Calendar.MILLISECOND, 0);
        Date startOfMonth2 = calendarMonth2.getTime();

        calendarMonth2.set(Calendar.DAY_OF_MONTH, calendarMonth2.getActualMaximum(Calendar.DAY_OF_MONTH)); // Cuối tháng
        calendarMonth2.set(Calendar.HOUR_OF_DAY, 23);
        calendarMonth2.set(Calendar.MINUTE, 59);
        calendarMonth2.set(Calendar.SECOND, 59);
        calendarMonth2.set(Calendar.MILLISECOND, 999);
        Date endOfMonth2 = calendarMonth2.getTime();

        Calendar calendarWeek3 = Calendar.getInstance();
        calendarWeek3.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendarWeek3.setFirstDayOfWeek(Calendar.MONDAY); // Bắt đầu tuần từ thứ Hai

        // Xác định đầu tuần và cuối tuần cua 2 tuan truoc
        calendarWeek3.add(Calendar.WEEK_OF_YEAR, -2);
        calendarWeek3.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // Đầu tuần
        calendarWeek3.set(Calendar.HOUR_OF_DAY, 0);
        calendarWeek3.set(Calendar.MINUTE, 0);
        calendarWeek3.set(Calendar.SECOND, 0);
        calendarWeek3.set(Calendar.MILLISECOND, 0);
        Date startOfWeek3 = calendarWeek3.getTime();

        calendarWeek3.add(Calendar.DAY_OF_WEEK, 6); // Cuối tuần
        calendarWeek3.set(Calendar.HOUR_OF_DAY, 23);
        calendarWeek3.set(Calendar.MINUTE, 59);
        calendarWeek3.set(Calendar.SECOND, 59);
        calendarWeek3.set(Calendar.MILLISECOND, 999);
        Date endOfWeek3 = calendarWeek3.getTime();

        Calendar calendarMonth3 = Calendar.getInstance();
        calendarMonth3.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendarMonth3.setFirstDayOfWeek(Calendar.MONDAY); // Bắt đầu tuần từ thứ Hai

        // Xác định đầu tháng và cuối tháng của thang truoc
        calendarMonth3.add(Calendar.MONTH, -2);
        calendarMonth3.set(Calendar.DAY_OF_MONTH, 1); // Đầu tháng
        calendarMonth3.set(Calendar.HOUR_OF_DAY, 0);
        calendarMonth3.set(Calendar.MINUTE, 0);
        calendarMonth3.set(Calendar.SECOND, 0);
        calendarMonth3.set(Calendar.MILLISECOND, 0);
        Date startOfMonth3 = calendarMonth3.getTime();

        calendarMonth3.set(Calendar.DAY_OF_MONTH, calendarMonth3.getActualMaximum(Calendar.DAY_OF_MONTH)); // Cuối tháng
        calendarMonth3.set(Calendar.HOUR_OF_DAY, 23);
        calendarMonth3.set(Calendar.MINUTE, 59);
        calendarMonth3.set(Calendar.SECOND, 59);
        calendarMonth3.set(Calendar.MILLISECOND, 999);
        Date endOfMonth3 = calendarMonth3.getTime();


        Calendar calendarYear1 = Calendar.getInstance();
        calendarYear1.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Date potholeDate = sdf.parse(date);
            Log.d("DATE","date" + potholeDate);
            if (potholeDate != null) {
                // Theo tuần
                if (!potholeDate.before(startOfWeek1) && !potholeDate.after(endOfWeek1)) {
                    calendarWeek1.setTime(potholeDate);
                    int dayOfWeekIndex = calendarWeek1.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
                    if (dayOfWeekIndex < 0)
                        dayOfWeekIndex = 6; // Chuyển Chủ Nhật thành cuối tuần (index 6)
                    weekData1[dayOfWeekIndex]--;
                }

                // Theo tháng
                if (!potholeDate.before(startOfMonth1) && !potholeDate.after(endOfMonth1)) {
                    calendarMonth1.setTime(potholeDate);
                    int weekOfMonthIndex = calendarMonth1.get(Calendar.WEEK_OF_MONTH) - 1;
                    monthData1[weekOfMonthIndex]--;
                }

                if (!potholeDate.before(startOfWeek2) && !potholeDate.after(endOfWeek2)) {
                    calendarWeek2.setTime(potholeDate);
                    int dayOfWeekIndex = calendarWeek2.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
                    if (dayOfWeekIndex < 0)
                        dayOfWeekIndex = 6; // Chuyển Chủ Nhật thành cuối tuần (index 6)
                    weekData2[dayOfWeekIndex]--;
                }

                // Theo tháng
                if (!potholeDate.before(startOfMonth2) && !potholeDate.after(endOfMonth2)) {
                    calendarMonth2.setTime(potholeDate);
                    int weekOfMonthIndex = calendarMonth2.get(Calendar.WEEK_OF_MONTH) - 1;
                    monthData2[weekOfMonthIndex]--;
                }

                if (!potholeDate.before(startOfWeek3) && !potholeDate.after(endOfWeek3)) {
                    calendarWeek3.setTime(potholeDate);
                    int dayOfWeekIndex = calendarWeek3.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
                    if (dayOfWeekIndex < 0)
                        dayOfWeekIndex = 6; // Chuyển Chủ Nhật thành cuối tuần (index 6)
                    weekData3[dayOfWeekIndex]--;
                }

                // Theo tháng
                if (!potholeDate.before(startOfMonth1) && !potholeDate.after(endOfMonth1)) {
                    calendarMonth3.setTime(potholeDate);
                    int weekOfMonthIndex = calendarMonth3.get(Calendar.WEEK_OF_MONTH) - 1;
                    monthData3[weekOfMonthIndex]--;
                }

                calendarYear1.setTime(potholeDate);
                int monthIndex = calendarYear1.get(Calendar.MONTH); // January=0
                yearData1[monthIndex]--;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        setupPieChart(danger, warning, caution);
        switch (typeTime){
            case 0:
                switch (timeAgo){
                    case 0:
                        setupBarChart(weekData1, labelw);
                        time.setText("This week");
                        break;
                    case 1:
                        setupBarChart(weekData2, labelw);
                        time.setText("1 week ago");
                        break;
                    case 2:
                        setupBarChart(weekData3, labelw);
                        time.setText("2 week ago");
                        break;
                }
                break;
            case 1:
                switch (timeAgo){
                    case 0:
                        setupBarChart(monthData1, labelm);
                        time.setText("This month");
                        break;
                    case 1:
                        setupBarChart(monthData2, labelm);
                        time.setText("1 month ago");
                        break;
                    case 2:
                        setupBarChart(monthData3, labelm);
                        time.setText("2 month ago");
                        break;
                }
                break;
            case 2:
                setupBarChart(yearData1, labely);
                time.setText("This year");
                break;
        }
    }

    public void ChangeUser(User newUser){
        this.user = newUser;
        name.setText(newUser.getName());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        pieChart = view.findViewById(R.id.dount_chart);
        user_profile = view.findViewById(R.id.image);
        barChart = view.findViewById(R.id.bar_chart);
        weekButton = view.findViewById(R.id.week);
        monthButton = view.findViewById(R.id.month);
        yearButton = view.findViewById(R.id.year);
        TextView daily = view.findViewById(R.id.daily);
        name = view.findViewById(R.id.name);
        dayscreate = view.findViewById(R.id.days);
        time = view.findViewById(R.id.time);
        pre = view.findViewById(R.id.pre);
        next = view.findViewById(R.id.next);


        apiService = ApiClient.getClient(isEmulator()).create(UserApiService.class);

        typeBarChart = 0;

        // Set up user profile navigation
        user_profile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Profile.class);
            intent.putExtra("name", user.getName());
            intent.putExtra("gender", user.getGender());
            intent.putExtra("birthday", user.getBirthday());
            String user_address = user.getAddress().getDistrict() + "\n " + user.getAddress().getProvince();
            Log.d("ADDRESS","Address: " + user_address);
            intent.putExtra("address", user_address);
            intent.putExtra("phone", user.getPhone());
            Log.d("PHONE","Phone: " + user.getPhone());
            startActivity(intent);
        });

        Home homeActivity = (Home) getActivity();
        if (homeActivity != null) {
            email = homeActivity.getUseremail();
        }

        fetchUserByEmail(email);
        fetchPotholesByAuthor(email);

        weekButton.setOnClickListener(v -> {
            if (typeTime != 0) {
                setupBarChart(weekData1, labelw);
                weekButton.setBackgroundResource(R.drawable.button);
                monthButton.setBackgroundResource(R.drawable.button_off);
                yearButton.setBackgroundResource(R.drawable.button_off);
                daily.setText("Weekly Pot Hole");
                time.setText("This week");
                typeTime = 0;
                timeAgo = 0;
            }
        });

        monthButton.setOnClickListener(v -> {
            if (typeTime != 1) {
                setupBarChart(monthData1, labelm);
                weekButton.setBackgroundResource(R.drawable.button_off);
                monthButton.setBackgroundResource(R.drawable.button);
                yearButton.setBackgroundResource(R.drawable.button_off);
                daily.setText("Monthly Pot Hole");
                time.setText("This month");
                typeTime = 1;
                timeAgo = 0;
            }
        });

        yearButton.setOnClickListener(v -> {
            if (typeTime != 2) {
                setupBarChart(yearData1, labely);
                weekButton.setBackgroundResource(R.drawable.button_off);
                monthButton.setBackgroundResource(R.drawable.button_off);
                yearButton.setBackgroundResource(R.drawable.button);
                daily.setText("Yearly Pot Hole");
                time.setText("This year");
                typeTime = 2;
                timeAgo = 0;
            }
        });

        pre.setOnClickListener( v -> {
            if (timeAgo != 2){
                timeAgo++;
            }
            changeBarChart(typeTime, timeAgo);
        });

        next.setOnClickListener( v -> {
            if (timeAgo != 0){
                timeAgo--;
            }
            changeBarChart(typeTime, timeAgo);
        });

        return view;
    }

    private void changeBarChart(Integer typeTime, Integer typeAgo) {
        switch (typeTime){
            case 0:
                switch (typeAgo){
                    case 0:
                        setupBarChart(weekData1, labelw);
                        time.setText("This week");
                        break;
                    case 1:
                        setupBarChart(weekData2, labelw);
                        time.setText("1 week ago");
                        break;
                    case 2:
                        setupBarChart(weekData3, labelw);
                        time.setText("2 week ago");
                        break;
                }
                break;
            case 1:
                switch (typeAgo){
                    case 0:
                        setupBarChart(monthData1, labelm);
                        time.setText("This month");
                        break;
                    case 1:
                        setupBarChart(monthData2, labelm);
                        time.setText("1 month ago");
                        break;
                    case 2:
                        setupBarChart(monthData3, labelm);
                        time.setText("2 month ago");
                        break;
                }
                break;
            case 2:
                setupBarChart(yearData1, labely);
                time.setText("This year");
                break;
        }
    }

    private void setupPieChart(Integer danger, Integer warning, Integer caution) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(caution.floatValue()));
        entries.add(new PieEntry(warning.floatValue()));
        entries.add(new PieEntry(danger.floatValue()));

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

        Integer total = danger + warning + caution;
        String total_potholes = "Total\n" + total;

        PieData data = new PieData(dataSet);

        // Configure the PieChart
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false); // Remove description
        pieChart.getLegend().setEnabled(false);      // Remove legend
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setCenterText(total_potholes);
        pieChart.setCenterTextSize(24f);
        pieChart.setCenterTextTypeface(Typeface.DEFAULT_BOLD);
        pieChart.animateY(1000);

        pieChart.invalidate(); // Refresh chart
    }

    private void setupBarChart(int[] data, String[] labels) {
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

    private void fetchUserByEmail(String email) {
        Call<ApiResponse> call = apiService.getUserByEmail(email);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body().isStatus()) {
                    user = response.body().getData();
                    if (user != null) {
                        String nameuser = user.getName();
                        String createuser = user.getCreate();
                        String createday = formatDateFromMongo(createuser);
                        name.setText(nameuser);
                        String pattern = "yyyy-MM-dd";
                        // Tính số ngày giữa hai thời điểm
                        long daysDifference = calculateDaysBetweenWithCurrent(createday, pattern);
                        String numberday = String.valueOf(daysDifference);
                        dayscreate.setText(numberday);
                        Toast.makeText(requireActivity(), user.getName(), Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("Error", "User is null");
                    }
                } else {
                    Log.e("Error", response.body() != null ? response.body().getMessage() : "Unknown error");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("API Error", t.getMessage());
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

    private void fetchPotholesByAuthor(String authorEmail) {
        apiService.getPotholes(authorEmail).enqueue(new Callback<PotholeResponse>() {
            @Override
            public void onResponse(Call<PotholeResponse> call, Response<PotholeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PotholeResponse potholeResponse = response.body();
                    potholes_user = potholeResponse.getPotholes();
                    if (potholes_user != null) {
                        Toast.makeText(requireActivity(), "Potholes size: " + potholes_user.size(), Toast.LENGTH_SHORT).show();
                        categorizePotholes();
                        setupPieChart(danger,warning,caution);
                        setupBarChart(weekData1, labelw);
                    } else {
                        Toast.makeText(requireActivity(), "Potholes list is empty", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(requireActivity(), "Failed to load potholes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PotholeResponse> call, Throwable t) {
                Toast.makeText(requireActivity(), "Error fetching potholes: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void categorizePotholes() {
        if (potholes_user != null && !potholes_user.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Calendar calendarWeek1 = Calendar.getInstance();
            calendarWeek1.setTimeZone(TimeZone.getTimeZone("UTC"));
            calendarWeek1.setFirstDayOfWeek(Calendar.MONDAY); // Bắt đầu tuần từ thứ Hai

            // Xác định đầu tuần và cuối tuần của tuần này
            calendarWeek1.add(Calendar.WEEK_OF_YEAR, 0);
            calendarWeek1.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // Đầu tuần
            calendarWeek1.set(Calendar.HOUR_OF_DAY, 0);
            calendarWeek1.set(Calendar.MINUTE, 0);
            calendarWeek1.set(Calendar.SECOND, 0);
            calendarWeek1.set(Calendar.MILLISECOND, 0);
            Date startOfWeek1 = calendarWeek1.getTime();

            calendarWeek1.add(Calendar.DAY_OF_WEEK, 6); // Cuối tuần
            calendarWeek1.add(Calendar.DAY_OF_WEEK, 6);
            calendarWeek1.set(Calendar.HOUR_OF_DAY, 23);
            calendarWeek1.set(Calendar.MINUTE, 59);
            calendarWeek1.set(Calendar.SECOND, 59);
            calendarWeek1.set(Calendar.MILLISECOND, 999);
            Date endOfWeek1 = calendarWeek1.getTime();

            Calendar calendarMonth1 = Calendar.getInstance();
            calendarMonth1.setTimeZone(TimeZone.getTimeZone("UTC"));
            calendarMonth1.setFirstDayOfWeek(Calendar.MONDAY); // Bắt đầu tuần từ thứ Hai

            // Xác định đầu tháng và cuối tháng của tháng này
            calendarMonth1.add(Calendar.MONTH, 0);
            calendarMonth1.set(Calendar.DAY_OF_MONTH, 1); // Đầu tháng
            calendarMonth1.set(Calendar.HOUR_OF_DAY, 0);
            calendarMonth1.set(Calendar.MINUTE, 0);
            calendarMonth1.set(Calendar.SECOND, 0);
            calendarMonth1.set(Calendar.MILLISECOND, 0);
            Date startOfMonth1 = calendarMonth1.getTime();

            calendarMonth1.set(Calendar.DAY_OF_MONTH, calendarMonth1.getActualMaximum(Calendar.DAY_OF_MONTH)); // Cuối tháng
            calendarMonth1.set(Calendar.HOUR_OF_DAY, 23);
            calendarMonth1.set(Calendar.MINUTE, 59);
            calendarMonth1.set(Calendar.SECOND, 59);
            calendarMonth1.set(Calendar.MILLISECOND, 999);
            Date endOfMonth1 = calendarMonth1.getTime();

            Calendar calendarWeek2 = Calendar.getInstance();
            calendarWeek2.setTimeZone(TimeZone.getTimeZone("UTC"));
            calendarWeek2.setFirstDayOfWeek(Calendar.MONDAY); // Bắt đầu tuần từ thứ Hai

            // Xác định đầu tuần và cuối tuần cua 1 tuan truoc
            calendarWeek2.add(Calendar.WEEK_OF_YEAR, -1);
            calendarWeek2.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // Đầu tuần
            calendarWeek2.set(Calendar.HOUR_OF_DAY, 0);
            calendarWeek2.set(Calendar.MINUTE, 0);
            calendarWeek2.set(Calendar.SECOND, 0);
            calendarWeek2.set(Calendar.MILLISECOND, 0);
            Date startOfWeek2 = calendarWeek2.getTime();

            calendarWeek2.add(Calendar.DAY_OF_WEEK, 6); // Cuối tuần
            calendarWeek2.set(Calendar.HOUR_OF_DAY, 23);
            calendarWeek2.set(Calendar.MINUTE, 59);
            calendarWeek2.set(Calendar.SECOND, 59);
            calendarWeek2.set(Calendar.MILLISECOND, 999);
            Date endOfWeek2 = calendarWeek2.getTime();

            Calendar calendarMonth2 = Calendar.getInstance();
            calendarMonth2.setTimeZone(TimeZone.getTimeZone("UTC"));
            calendarMonth2.setFirstDayOfWeek(Calendar.MONDAY); // Bắt đầu tuần từ thứ Hai

            // Xác định đầu tháng và cuối tháng cua 1 thang truoc
            calendarMonth2.add(Calendar.MONTH, -1);
            calendarMonth2.set(Calendar.DAY_OF_MONTH, 1); // Đầu tháng
            calendarMonth2.set(Calendar.HOUR_OF_DAY, 0);
            calendarMonth2.set(Calendar.MINUTE, 0);
            calendarMonth2.set(Calendar.SECOND, 0);
            calendarMonth2.set(Calendar.MILLISECOND, 0);
            Date startOfMonth2 = calendarMonth2.getTime();

            calendarMonth2.set(Calendar.DAY_OF_MONTH, calendarMonth2.getActualMaximum(Calendar.DAY_OF_MONTH)); // Cuối tháng
            calendarMonth2.set(Calendar.HOUR_OF_DAY, 23);
            calendarMonth2.set(Calendar.MINUTE, 59);
            calendarMonth2.set(Calendar.SECOND, 59);
            calendarMonth2.set(Calendar.MILLISECOND, 999);
            Date endOfMonth2 = calendarMonth2.getTime();

            Calendar calendarWeek3 = Calendar.getInstance();
            calendarWeek3.setTimeZone(TimeZone.getTimeZone("UTC"));
            calendarWeek3.setFirstDayOfWeek(Calendar.MONDAY); // Bắt đầu tuần từ thứ Hai

            // Xác định đầu tuần và cuối tuần cua 2 tuan truoc
            calendarWeek3.add(Calendar.WEEK_OF_YEAR, -2);
            calendarWeek3.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // Đầu tuần
            calendarWeek3.set(Calendar.HOUR_OF_DAY, 0);
            calendarWeek3.set(Calendar.MINUTE, 0);
            calendarWeek3.set(Calendar.SECOND, 0);
            calendarWeek3.set(Calendar.MILLISECOND, 0);
            Date startOfWeek3 = calendarWeek3.getTime();

            calendarWeek3.add(Calendar.DAY_OF_WEEK, 6); // Cuối tuần
            calendarWeek3.set(Calendar.HOUR_OF_DAY, 23);
            calendarWeek3.set(Calendar.MINUTE, 59);
            calendarWeek3.set(Calendar.SECOND, 59);
            calendarWeek3.set(Calendar.MILLISECOND, 999);
            Date endOfWeek3 = calendarWeek3.getTime();

            Calendar calendarMonth3 = Calendar.getInstance();
            calendarMonth3.setTimeZone(TimeZone.getTimeZone("UTC"));
            calendarMonth3.setFirstDayOfWeek(Calendar.MONDAY); // Bắt đầu tuần từ thứ Hai

            // Xác định đầu tháng và cuối tháng của thang truoc
            calendarMonth3.add(Calendar.MONTH, -2);
            calendarMonth3.set(Calendar.DAY_OF_MONTH, 1); // Đầu tháng
            calendarMonth3.set(Calendar.HOUR_OF_DAY, 0);
            calendarMonth3.set(Calendar.MINUTE, 0);
            calendarMonth3.set(Calendar.SECOND, 0);
            calendarMonth3.set(Calendar.MILLISECOND, 0);
            Date startOfMonth3 = calendarMonth3.getTime();

            calendarMonth3.set(Calendar.DAY_OF_MONTH, calendarMonth3.getActualMaximum(Calendar.DAY_OF_MONTH)); // Cuối tháng
            calendarMonth3.set(Calendar.HOUR_OF_DAY, 23);
            calendarMonth3.set(Calendar.MINUTE, 59);
            calendarMonth3.set(Calendar.SECOND, 59);
            calendarMonth3.set(Calendar.MILLISECOND, 999);
            Date endOfMonth3 = calendarMonth3.getTime();


            Calendar calendarYear1 = Calendar.getInstance();
            calendarYear1.setTimeZone(TimeZone.getTimeZone("UTC"));

            // Biến đếm số lượng theo mức độ
            danger = 0;
            warning = 0;
            caution = 0;

            // Duyệt qua danh sách potholes
            for (PotholeClass pothole : potholes_user) {
                if (pothole != null) {
                    // Phân loại pothole theo type
                    switch (pothole.getType()) {
                        case 1:
                            caution++;
                            break;
                        case 2:
                            warning++;
                            break;
                        case 3:
                            danger++;
                            break;
                        default:
                            // Không xử lý type khác
                            break;
                    }

                    try {
                        // Phân tích dữ liệu theo tuần, tháng và năm
                        Date potholeDate = sdf.parse(pothole.getDate());
                        if (potholeDate != null) {
                            // Theo tuần
                            if (!potholeDate.before(startOfWeek1) && !potholeDate.after(endOfWeek1)) {
                                calendarWeek1.setTime(potholeDate);
                                int dayOfWeekIndex = calendarWeek1.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
                                if (dayOfWeekIndex < 0) dayOfWeekIndex = 6; // Chuyển Chủ Nhật thành cuối tuần (index 6)
                                weekData1[dayOfWeekIndex]++;
                            }

                            // Theo tháng
                            if (!potholeDate.before(startOfMonth1) && !potholeDate.after(endOfMonth1)) {
                                calendarMonth1.setTime(potholeDate);
                                int weekOfMonthIndex = calendarMonth1.get(Calendar.WEEK_OF_MONTH) - 1;
                                monthData1[weekOfMonthIndex]++;
                            }

                            if (!potholeDate.before(startOfWeek2) && !potholeDate.after(endOfWeek2)) {
                                calendarWeek2.setTime(potholeDate);
                                int dayOfWeekIndex = calendarWeek2.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
                                if (dayOfWeekIndex < 0) dayOfWeekIndex = 6; // Chuyển Chủ Nhật thành cuối tuần (index 6)
                                weekData2[dayOfWeekIndex]++;
                            }

                            // Theo tháng
                            if (!potholeDate.before(startOfMonth2) && !potholeDate.after(endOfMonth2)) {
                                calendarMonth2.setTime(potholeDate);
                                int weekOfMonthIndex = calendarMonth2.get(Calendar.WEEK_OF_MONTH) - 1;
                                monthData2[weekOfMonthIndex]++;
                            }

                            if (!potholeDate.before(startOfWeek3) && !potholeDate.after(endOfWeek3)) {
                                calendarWeek3.setTime(potholeDate);
                                int dayOfWeekIndex = calendarWeek3.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
                                if (dayOfWeekIndex < 0) dayOfWeekIndex += 7; // Chuyển Chủ Nhật thành cuối tuần (index 6)
                                weekData3[dayOfWeekIndex]++;
                            }

                            // Theo tháng
                            if (!potholeDate.before(startOfMonth3) && !potholeDate.after(endOfMonth3)) {
                                calendarMonth3.setTime(potholeDate);
                                int weekOfMonthIndex = calendarMonth3.get(Calendar.WEEK_OF_MONTH) - 1;
                                monthData3[weekOfMonthIndex]++;
                            }

                            // Theo năm
                            calendarYear1.setTime(potholeDate);
                            int monthIndex = calendarYear1.get(Calendar.MONTH); // January=0
                            yearData1[monthIndex]++;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Hiển thị kết quả
            Toast.makeText(requireActivity(),
                    "Caution: " + caution + ", Warning: " + warning + ", Danger: " + danger,
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireActivity(), "No potholes found or list is empty", Toast.LENGTH_SHORT).show();
        }
    }


    private long calculateDaysBetweenWithCurrent(String create, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            // Chuyển đổi ngày bắt đầu từ chuỗi
            Date startDate = sdf.parse(create);

            // Lấy ngày hiện tại
            Date currentDate = new Date();

            // Tính số ngày giữa hai thời điểm
            long diffInMillis = Math.abs(currentDate.getTime() - startDate.getTime());
            return diffInMillis / (24 * 60 * 60 * 1000) + 1;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private String formatDateFromMongo(String invalidDate) {
        try {
            // Tách các thành phần trong chuỗi ngày sai định dạng
            String[] parts = invalidDate.split("-");
            if (parts.length == 3) {
                String day = parts[0];
                String month = parts[1];
                String year = parts[2].substring(parts[2].length() - 4); // Lấy 4 số cuối của năm

                return year + "-" + month + "-" + day;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Invalid date";
    }

    public User getUser(){
        return user;
    }
}
