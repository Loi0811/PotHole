package com.example.pothole;

import static android.content.Context.MODE_PRIVATE;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.SharedPreferences;

public class Setting extends Fragment {

    public Setting() {
        // Required empty public constructor
    }
    private LinearLayout logout, editProfileItem, changePasswordItem, notificationItem, sensiticityItem, langugeItem, term_PoliciesItem, q_AItem, reportItem;
    private GoogleSignInOptions gso;
    private GoogleSignInClient gsc;
    private Spinner spinnerGender;
    private TextView tvBirthday;
    private Button btnSave;
    private Calendar calendar = Calendar.getInstance();
    private EditText etPhone, etDistrict, etProvince, etName;
    private ImageView backIcon;
    private UserApiService apiService;
    private User user_profile;
    private UserSetting listener;
    private boolean isPasswordVisible0 = false;
    private boolean isPasswordVisible1 = false;
    private boolean isPasswordVisible2 = false;
    private boolean isVibration, isSound, isSensor;
    private String language;
    private EditText passwordField0, passwordField1, passwordField2;
    private SharedPreferences sharedPreferences;

    private String email, password;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Setting.UserSetting) {
            listener = (Setting.UserSetting) context; // Gắn kết Activity với Interface
        } else {
            throw new RuntimeException(context.toString() + " must implement Update");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(requireActivity(),gso);

        apiService = ApiClient.getClient(isEmulator()).create(UserApiService.class);

        sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        if (!sharedPreferences.contains("vibration")) {
            sharedPreferences.edit().putBoolean("vibration", true).apply();
            isVibration = true;
        } else {
            isVibration = sharedPreferences.getBoolean("vibration", true);
        }
        if (!sharedPreferences.contains("sound")) {
            sharedPreferences.edit().putBoolean("sound", true).apply();
            isSound = true;
        } else {
            isSound = sharedPreferences.getBoolean("sound", true);
        }
        if (!sharedPreferences.contains("sensor")) {
            sharedPreferences.edit().putBoolean("sensor", true).apply();
            isSensor = true;
        } else {
            isSensor = sharedPreferences.getBoolean("sensor", true);
        }
        if (!sharedPreferences.contains("language")) {
            sharedPreferences.edit().putString("language", "English").apply();
            language = "en";
        } else {
            language = sharedPreferences.getString("language", "English");
        }


        logout = view.findViewById(R.id.logOutItem);
        logout.setOnClickListener(v -> {
            signOut();
        });

        Home homeActivity = (Home) getActivity();
        if (homeActivity != null) {
            user_profile = homeActivity.getUser();
            email = homeActivity.getUseremail();
            password = homeActivity.getUserPassword();
        }

        editProfileItem = view.findViewById(R.id.editProfileItem);
        editProfileItem.setOnClickListener(v -> showEditProfilePopup(v));

        changePasswordItem = view.findViewById(R.id.changePasswordItem);
        changePasswordItem.setOnClickListener(v -> showChangePasswordPopup(v));

        notificationItem = view.findViewById(R.id.notificationItem);
        notificationItem.setOnClickListener(v -> showNotificationPopup(v));

        sensiticityItem = view.findViewById(R.id.sensitivityItem);
        sensiticityItem.setOnClickListener(v -> showSensitivityPopup(v));

        langugeItem = view.findViewById(R.id.languageItem);
        langugeItem.setOnClickListener(v -> showLanguagePopup(v));

        term_PoliciesItem = view.findViewById(R.id.terms_n_PoliciesItem);
        term_PoliciesItem.setOnClickListener(v -> showTermsPoliciesPopup(v));

        q_AItem= view.findViewById(R.id.q_n_AItem);
        q_AItem.setOnClickListener(v -> showQAPopup(v));

        reportItem= view.findViewById(R.id.report_ProblemItem);
        reportItem.setOnClickListener(v -> showReportProblemPopup(v));

        return view;
    }

    void signOut(){
        gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    private void showEditProfilePopup(View anchorView) {
        // Inflate layout popup
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.edit_profile, null);

        // Tạo PopupWindow
        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true);



        // Xử lý nội dung trong popup
        etName = popupView.findViewById(R.id.username);
        etPhone = popupView.findViewById(R.id.phone);
        spinnerGender = popupView.findViewById(R.id.gender);
        etDistrict = popupView.findViewById(R.id.district);
        etProvince = popupView.findViewById(R.id.province);
        tvBirthday = popupView.findViewById(R.id.birthday);
        btnSave = popupView.findViewById(R.id.done);
        backIcon = popupView.findViewById(R.id.back);

        if (user_profile != null) {
            etName.setText(user_profile.getName());
            etPhone.setText(user_profile.getPhone());
            tvBirthday.setText(user_profile.getBirthday());

            if (user_profile.getAddress() != null) {
                etDistrict.setText(user_profile.getAddress().getDistrict());
                etProvince.setText(user_profile.getAddress().getProvince());
            }

            // Gán giá trị cho Spinner Gender
            setDefaultGender(user_profile.getGender());
        }
        tvBirthday.setOnClickListener(v -> showDatePickerDialog());

        backIcon.setOnClickListener(v -> {
            popupWindow.dismiss(); // Đóng popup
        });

        btnSave.setOnClickListener(v -> {
            updateUser(user_profile.getEmail());

            popupWindow.dismiss();
        });

        // Hiển thị PopupWindow tại vị trí anchorView
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Đặt nền trong suốt
        popupWindow.setElevation(10); // Hiệu ứng độ cao
        popupWindow.showAsDropDown(anchorView, 0, 20); // Hiển thị bên dưới `anchorView`
    }

    private void showChangePasswordPopup(View anchorView) {
        // Inflate layout popup
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.change_password, null);

        // Tạo PopupWindow
        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true);

        // Xử lý nội dung trong popup
        ImageView backIcon = popupView.findViewById(R.id.back);
        ImageView eyeIcon0 = popupView.findViewById(R.id.eye0);
        ImageView eyeIcon1 = popupView.findViewById(R.id.eye1);
        ImageView eyeIcon2 = popupView.findViewById(R.id.eye2);
        passwordField0 = popupView.findViewById(R.id.pwd0);
        passwordField1 = popupView.findViewById(R.id.pwd1);
        passwordField2 = popupView.findViewById(R.id.pwd2);
        Button change = popupView.findViewById(R.id.change);

        eyeIcon0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible0) {
                    // Hide password
                    passwordField0.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    eyeIcon0.setImageResource(R.drawable.ic_eye_foreground); // Use a closed eye icon if available
                    isPasswordVisible0 = false;
                } else {
                    // Show password
                    passwordField0.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    eyeIcon0.setImageResource(R.drawable.ic_close_eye); // Use an open eye icon if available
                    isPasswordVisible0 = true;
                }
                // Move cursor to the end of the text
                passwordField0.setSelection(passwordField0.getText().length());
            }
        });

        eyeIcon1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible1) {
                    // Hide password
                    passwordField1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    eyeIcon1.setImageResource(R.drawable.ic_eye_foreground); // Use a closed eye icon if available
                    isPasswordVisible1 = false;
                } else {
                    // Show password
                    passwordField1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    eyeIcon1.setImageResource(R.drawable.ic_close_eye); // Use an open eye icon if available
                    isPasswordVisible1 = true;
                }
                // Move cursor to the end of the text
                passwordField1.setSelection(passwordField1.getText().length());
            }
        });

        // Eye icon for passwordField2: Toggle password visibility
        eyeIcon2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible2) {
                    // Hide password
                    passwordField2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    eyeIcon2.setImageResource(R.drawable.ic_eye_foreground); // Use a closed eye icon if available
                    isPasswordVisible2 = false;
                } else {
                    // Show password
                    passwordField2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    eyeIcon2.setImageResource(R.drawable.ic_close_eye); // Use an open eye icon if available
                    isPasswordVisible2 = true;
                }
                // Move cursor to the end of the text
                passwordField2.setSelection(passwordField2.getText().length());
            }
        });

        backIcon.setOnClickListener(v -> {
            popupWindow.dismiss(); // Đóng popup
        });

        change.setOnClickListener(v -> {
            String oldPassword = passwordField0.getText().toString().trim();
            String newPassword1 = passwordField1.getText().toString().trim();
            String newPassword2 = passwordField2.getText().toString().trim();

            if (oldPassword.isEmpty() || newPassword1.isEmpty() || newPassword2.isEmpty()) {
                Toast.makeText(requireActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword1.equals(newPassword2)) {
                Toast.makeText(requireActivity(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!oldPassword.equals(password)) {
                Toast.makeText(requireActivity(), "Old passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }


            updatePassword(user_profile.getEmail(), newPassword1);
            popupWindow.dismiss();
        });

        // Hiển thị PopupWindow tại vị trí anchorView
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Đặt nền trong suốt
        popupWindow.setElevation(10); // Hiệu ứng độ cao
        popupWindow.showAsDropDown(anchorView, 0, 20); // Hiển thị bên dưới `anchorView`
    }

    private void showNotificationPopup(View anchorView) {
        // Inflate layout popup
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.notification, null);

        // Tạo PopupWindow
        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true);

        ImageView backIcon = popupView.findViewById(R.id.back);
        Button save = popupView.findViewById(R.id.save);

        Switch switchVibration = popupView.findViewById(R.id.switch_vibration);
        Switch switchSound = popupView.findViewById(R.id.switch_sound);

        switchVibration.setChecked(isVibration);
        switchSound.setChecked(isSound);

        backIcon.setOnClickListener(v -> {
            popupWindow.dismiss(); // Đóng popup
        });

        save.setOnClickListener(v -> {
            sharedPreferences.edit()
                    .putBoolean("vibration", switchVibration.isChecked())
                    .putBoolean("sound", switchSound.isChecked())
                    .apply();
            isVibration = switchVibration.isChecked();
            isSound = switchSound.isChecked();
            popupWindow.dismiss();
        });

        // Hiển thị PopupWindow tại vị trí anchorView
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Đặt nền trong suốt
        popupWindow.setElevation(10); // Hiệu ứng độ cao
        popupWindow.showAsDropDown(anchorView, 0, 20); // Hiển thị bên dưới `anchorView`
    }

    private void showSensitivityPopup(View anchorView) {
        // Inflate layout popup
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.sensitivity, null);

        // Tạo PopupWindow
        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true);

        ImageView backIcon = popupView.findViewById(R.id.back);
        Button save = popupView.findViewById(R.id.save);

        Switch switchSensor = popupView.findViewById(R.id.switch_sensor);

        switchSensor.setChecked(isSensor);

        backIcon.setOnClickListener(v -> {
            popupWindow.dismiss(); // Đóng popup
        });

        save.setOnClickListener(v -> {
            sharedPreferences.edit()
                    .putBoolean("sensor", switchSensor.isChecked())
                    .apply();
            isSensor = switchSensor.isChecked();
            popupWindow.dismiss();
        });

        // Hiển thị PopupWindow tại vị trí anchorView
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Đặt nền trong suốt
        popupWindow.setElevation(10); // Hiệu ứng độ cao
        popupWindow.showAsDropDown(anchorView, 0, 20); // Hiển thị bên dưới `anchorView`
    }

    private void showLanguagePopup(View anchorView) {
        // Inflate layout popup
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.language, null);

        // Tạo PopupWindow
        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true);

        // Lấy các thành phần UI
        ImageView backIcon = popupView.findViewById(R.id.back);
        Spinner spinnerLanguage = popupView.findViewById(R.id.spinner_language);
        Button saveButton = popupView.findViewById(R.id.save);

        // Truy cập SharedPreferences

        // Thiết lập ngôn ngữ ban đầu cho Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.languages, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);

        // Đặt giá trị ban đầu cho Spinner
        int spinnerPosition = adapter.getPosition(language);
        spinnerLanguage.setSelection(spinnerPosition);

        // Xử lý nút back
        backIcon.setOnClickListener(v -> popupWindow.dismiss());

        // Xử lý nút lưu
        saveButton.setOnClickListener(v -> {
            // Lấy giá trị ngôn ngữ được chọn từ Spinner
            String selectedLanguage = spinnerLanguage.getSelectedItem().toString();

            // Lưu giá trị vào SharedPreferences
            sharedPreferences.edit()
                    .putString("language", selectedLanguage)
                    .apply();
            language = selectedLanguage;
            // Đóng popup sau khi lưu
            popupWindow.dismiss();
        });

        // Hiển thị PopupWindow tại vị trí anchorView
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Đặt nền trong suốt
        popupWindow.setElevation(10); // Hiệu ứng độ cao
        popupWindow.showAsDropDown(anchorView, 0, 20); // Hiển thị bên dưới anchorView
    }

    private void showTermsPoliciesPopup(View anchorView){
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.terms_policies, null);

        // Tạo PopupWindow
        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true);

        ImageView backIcon = popupView.findViewById(R.id.back);
        backIcon.setOnClickListener(v -> {
            popupWindow.dismiss(); // Đóng popup
        });


        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Đặt nền trong suốt
        popupWindow.setElevation(10); // Hiệu ứng độ cao
        popupWindow.showAsDropDown(anchorView, 0, 20);
    }

    private void showReportProblemPopup(View anchorView){
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.report, null);

        // Tạo PopupWindow
        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true);

        ImageView backIcon = popupView.findViewById(R.id.back);
        Button send = popupView.findViewById(R.id.send);
        EditText report_text = popupView.findViewById(R.id.report);
        backIcon.setOnClickListener(v -> {
            popupWindow.dismiss(); // Đóng popup
        });
        send.setOnClickListener(v -> {
            popupWindow.dismiss(); // Đóng popup
        });


        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Đặt nền trong suốt
        popupWindow.setElevation(10); // Hiệu ứng độ cao
        popupWindow.showAsDropDown(anchorView, 0, 20);
    }


    private void showQAPopup(View anchorView){
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.q_a, null);

        // Tạo PopupWindow
        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true);

        ImageView backIcon = popupView.findViewById(R.id.back);
        backIcon.setOnClickListener(v -> {
            popupWindow.dismiss(); // Đóng popup
        });

        TextView question1 = popupView.findViewById(R.id.question1);
        LinearLayout answer1 = popupView.findViewById(R.id.answer1);

        TextView question2 = popupView.findViewById(R.id.question2);
        LinearLayout answer2 = popupView.findViewById(R.id.answer2);

        TextView question3 = popupView.findViewById(R.id.question3);
        LinearLayout answer3 = popupView.findViewById(R.id.answer3);

        TextView question4 = popupView.findViewById(R.id.question4);
        LinearLayout answer4 = popupView.findViewById(R.id.answer4);

        TextView question5 = popupView.findViewById(R.id.question5);
        LinearLayout answer5 = popupView.findViewById(R.id.answer5);

        question1.setOnClickListener(v -> toggleVisibility(answer1,question1));
        question2.setOnClickListener(v -> toggleVisibility(answer2,question2));
        question3.setOnClickListener(v -> toggleVisibility(answer3,question3));
        question4.setOnClickListener(v -> toggleVisibility(answer4,question4));
        question5.setOnClickListener(v -> toggleVisibility(answer5,question5));


        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Đặt nền trong suốt
        popupWindow.setElevation(10); // Hiệu ứng độ cao
        popupWindow.showAsDropDown(anchorView, 0, 20);
    }

    private void toggleVisibility(LinearLayout answerLayout, TextView question) {
        if (answerLayout.getVisibility() == View.GONE) {
            question.setBackground(getResources().getDrawable(R.drawable.question));
            answerLayout.setVisibility(View.VISIBLE);
        } else {
            answerLayout.setVisibility(View.GONE);
            question.setBackground(getResources().getDrawable(R.drawable.question0));
        }
    }


    public static boolean isEmulator() {
        return Build.FINGERPRINT.contains("generic") ||
                Build.MODEL.contains("Emulator") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                "google_sdk".equals(Build.PRODUCT);
    }

    private void showDatePickerDialog() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireActivity(),
                (view, year1, month1, dayOfMonth) -> {
                    String selectedDate = (month1 + 1) + "-" + dayOfMonth + "-" + year1;
                    tvBirthday.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    public interface UserSetting {
        void SetUser(User user);
    }

    private void setDefaultGender(String gender) {
        if (gender != null) {
            String[] genderOptions = getResources().getStringArray(R.array.gender_options);
            for (int i = 0; i < genderOptions.length; i++) {
                if (gender.equalsIgnoreCase(genderOptions[i])) {
                    spinnerGender.setSelection(i); // Chọn giá trị tương ứng
                    break;
                }
            }
        }
    }

    private void updateUser(String email) {
        String phone = etPhone.getText().toString();
        String gender = spinnerGender.getSelectedItem().toString();
        String district = etDistrict.getText().toString();
        String province = etProvince.getText().toString();
        String birthday = tvBirthday.getText().toString();
        String name = etName.getText().toString();

        user_profile.setPhone(phone);
        user_profile.setGender(gender);
        user_profile.setName(name);
        user_profile.setBirthday(birthday);

        AddressUser addressUser = new AddressUser();
        addressUser.setDistrict(district);
        addressUser.setProvince(province);

        user_profile.setAddress(addressUser);

        AddressUser address = new AddressUser();
        address.setDistrict(district);
        address.setProvince(province);

        Userupdate user = new Userupdate();
        user.setName(name);
        user.setPhone(phone);
        user.setAddress(address);
        user.setBirthday(birthday);
        user.setGender(gender);

        apiService.updateUser(email, user).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isStatus()) {
                        Toast.makeText(requireActivity(), "User updated successfully", Toast.LENGTH_SHORT).show();
                        if (listener != null){
                            listener.SetUser(user_profile);
                        }
                    } else {
                        Toast.makeText(requireActivity(), apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireActivity(), "Failed to update user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(requireActivity(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updatePassword(String email, String newPassword1) {

        PasswordUpdateRequest request = new PasswordUpdateRequest(email, newPassword1);
        apiService.updatePassword(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse != null && apiResponse.isStatus()) {
                        Toast.makeText(requireActivity(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireActivity(), apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireActivity(), "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(requireActivity(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null; // Hủy liên kết khi Fragment bị tháo
    }

}

