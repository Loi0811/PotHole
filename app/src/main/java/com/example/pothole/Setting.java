package com.example.pothole;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
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

public class Setting extends Fragment {

    public Setting() {
        // Required empty public constructor
    }
    private LinearLayout logout, editProfileItem;
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

        logout = view.findViewById(R.id.logOutItem);
        logout.setOnClickListener(v -> {
            signOut();
        });

        Home homeActivity = (Home) getActivity();
        if (homeActivity != null) {
            user_profile = homeActivity.getUser();
        }

        editProfileItem = view.findViewById(R.id.editProfileItem);
        editProfileItem.setOnClickListener(v -> showEditProfilePopup(v));

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

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null; // Hủy liên kết khi Fragment bị tháo
    }

}

