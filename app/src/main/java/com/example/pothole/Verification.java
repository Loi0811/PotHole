package com.example.pothole;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Timer;

import android.os.AsyncTask;

import java.util.Random;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Verification extends AppCompatActivity {

    private EditText otpDigit1, otpDigit2, otpDigit3, otpDigit4;
    private TextView secCountdown, resendCode;
    private Button verifyButton;
    private ImageView backIcon, homeIcon;
    private CountDownTimer countDownTimer;
    private static final long START_TIME_IN_MILLIS = 60000; // 60 seconds

    private String userEmail; // Email nhận OTP
    private String generatedOtp; // Lưu OTP đã tạo

    private boolean isCountdownFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        otpDigit1 = findViewById(R.id.otpDigit1);
        otpDigit2 = findViewById(R.id.otpDigit2);
        otpDigit3 = findViewById(R.id.otpDigit3);
        otpDigit4 = findViewById(R.id.otpDigit4);
        secCountdown = findViewById(R.id.sec);
        resendCode = findViewById(R.id.resend);
        verifyButton = findViewById(R.id.login);
        backIcon = findViewById(R.id.back);
        homeIcon = findViewById(R.id.home);

        userEmail = getIntent().getStringExtra("sendemail");

        // Gửi OTP đến email khi Activity được khởi tạo
        generatedOtp = generateOtp();
        sendOtpEmail(userEmail, generatedOtp);

        startCountdown();

        // Add TextWatchers to move focus automatically between OTP fields
        setupOtpFields();

        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to the previous activity
            }
        });

        // Home icon action: Navigate to the MainActivity
        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Verification.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        resendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetCountdown();
                generatedOtp = generateOtp();
                sendOtpEmail(userEmail, generatedOtp);
                Toast.makeText(Verification.this, "Code resent", Toast.LENGTH_SHORT).show();
            }
        });

        verifyButton.setOnClickListener(v -> {
            if (!isCountdownFinished) { // Kiểm tra countdown trước khi xác thực
                String otp = otpDigit1.getText().toString() + otpDigit2.getText().toString() +
                        otpDigit3.getText().toString() + otpDigit4.getText().toString();
                if (otp.equals(generatedOtp)) {
                    Toast.makeText(Verification.this, "Verification Success", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Verification.this, ChangePassword.class);
                    intent.putExtra("useremail", userEmail);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(Verification.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(Verification.this, "OTP has expired. Please resend the code.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startCountdown() {
        isCountdownFinished = false; // Countdown bắt đầu
        resendCode.setEnabled(false);
        countDownTimer = new CountDownTimer(START_TIME_IN_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                secCountdown.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                secCountdown.setText("0");
                isCountdownFinished = true; // Countdown kết thúc
                resendCode.setEnabled(true);
            }
        }.start();
    }

    private void resetCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        secCountdown.setText("60");
        startCountdown();
    }

    private void setupOtpFields() {
        otpDigit1.addTextChangedListener(new OtpTextWatcher(otpDigit1, null, otpDigit2));
        otpDigit2.addTextChangedListener(new OtpTextWatcher(otpDigit2, otpDigit1, otpDigit3));
        otpDigit3.addTextChangedListener(new OtpTextWatcher(otpDigit3, otpDigit2, otpDigit4));
        otpDigit4.addTextChangedListener(new OtpTextWatcher(otpDigit4, otpDigit3, null));
    }

    private class OtpTextWatcher implements TextWatcher {
        private final EditText currentView;
        private final EditText previousView;
        private final EditText nextView;

        public OtpTextWatcher(EditText currentView, EditText previousView, EditText nextView) {
            this.currentView = currentView;
            this.previousView = previousView;
            this.nextView = nextView;

            // Listen for delete key presses on the current view to move focus backward
            currentView.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (currentView.getText().toString().isEmpty() && previousView != null) {
                        previousView.requestFocus();
                        previousView.setText(""); // Clear the previous field
                    }
                }
                return false;
            });
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 1 && nextView != null) {
                nextView.requestFocus();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }


    private String generateOtp() {
        Random random = new Random();
        int otp = 1000 + random.nextInt(9000); // Tạo mã OTP ngẫu nhiên từ 1000 đến 9999
        return String.valueOf(otp);
    }

    private void sendOtpEmail(String recipientEmail, String otp) {
        AsyncTask.execute(() -> {
            try {
                String senderEmail = "apptest1470@gmail.com";
                String senderPassword = "knsmmdzkbqkezcyt";

                Properties props = new Properties();
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.socketFactory.port", "465");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.port", "465");

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(senderEmail, senderPassword);
                    }
                });

                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(senderEmail));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
                message.setSubject("Your Verification Code");
                message.setText("Your OTP is: " + otp);

                Transport.send(message);
            } catch (MessagingException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(Verification.this, "Failed to send email", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
