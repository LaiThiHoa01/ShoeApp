package com.example.shoeapp.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shoeapp.R;
import com.example.shoeapp.data.entity.User;
import com.example.shoeapp.user.MainActivity;
import java.util.Random;

public class VerifyEmailOtpActivity extends AppCompatActivity {
    public static final String EXTRA_MODE = "mode";
    public static final String MODE_SIGN_UP = "sign_up";
    public static final String MODE_RESET_PASSWORD = "reset_password";

    public static final String EXTRA_FIRST_NAME = "first_name";
    public static final String EXTRA_LAST_NAME = "last_name";
    public static final String EXTRA_EMAIL = "email";
    public static final String EXTRA_PHONE = "phone";
    public static final String EXTRA_PASSWORD = "password";
    public static final String EXTRA_NEW_PASSWORD = "new_password";
    public static final String EXTRA_CONFIRM_PASSWORD = "confirm_password";

    private String mode, newPassword, confirmPassword,firstName,lastName,email,phone,password, currentOtp;
    private EditText otpInput;
    private TextView otpSubtitleText;
    private long otpExpiredAt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email_otp);

        readExtras();
        if (email == null || email.trim().isEmpty()) {
            Toast.makeText(this, "Chưa nhập email", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (MODE_SIGN_UP.equals(mode)) {
            if (firstName == null || lastName == null || phone == null || password == null) {
                Toast.makeText(this, "Thiếu thông tin đăng ký", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        if (MODE_RESET_PASSWORD.equals(mode)) {
            if (newPassword == null || confirmPassword == null) {
                Toast.makeText(this, "Thiếu thông tin đặt lại mật khẩu", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }
        bindViews();

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        otpSubtitleText.setText("Nhập mã OTP đã được gửi tới: " + email);

        findViewById(R.id.resendOtpText).setOnClickListener(v -> sendOtp());
        findViewById(R.id.verifyOtpButton).setOnClickListener(v -> verifyOtpAndContinue());
        sendOtp();
    }

    private void readExtras() {
        Intent intent = getIntent();

        mode = intent.getStringExtra(EXTRA_MODE);

        firstName = intent.getStringExtra(EXTRA_FIRST_NAME);
        lastName = intent.getStringExtra(EXTRA_LAST_NAME);
        email = intent.getStringExtra(EXTRA_EMAIL);
        phone = intent.getStringExtra(EXTRA_PHONE);
        password = intent.getStringExtra(EXTRA_PASSWORD);

        newPassword = intent.getStringExtra(EXTRA_NEW_PASSWORD);
        confirmPassword = intent.getStringExtra(EXTRA_CONFIRM_PASSWORD);

        if (mode == null) {
            mode = MODE_SIGN_UP;
        }
    }

    private void bindViews() {
        otpInput = findViewById(R.id.otpInput);
        otpSubtitleText = findViewById(R.id.otpSubtitleText);
    }

    private void sendOtp() {
        currentOtp = generateOtp();
        otpExpiredAt = System.currentTimeMillis() + 5 * 60 * 1000;

        Toast.makeText(this, "Đang gửi OTP...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                EmailOtp.sendOtp(email, currentOtp);

                runOnUiThread(() ->
                        Toast.makeText(this, "Đã gửi OTP về email", Toast.LENGTH_SHORT).show()
                );

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Gửi OTP thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    private void verifyOtpAndContinue() {
        String inputOtp = otpInput.getText().toString().trim();

        if (System.currentTimeMillis() > otpExpiredAt) {
            Toast.makeText(this, "OTP đã hết hạn", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!currentOtp.equals(inputOtp)) {
            otpInput.setError("OTP không đúng");
            return;
        }

        if (MODE_RESET_PASSWORD.equals(mode)) {
            resetPasswordAfterOtp();
        } else {
            registerAfterOtp();
        }
    }

    private void registerAfterOtp() {
        try {
            AuthRepository authRepository = new AuthRepository(this);
            User user = authRepository.register(firstName, lastName, email, phone, password);

            Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
            SessionManager.saveSession(this, user.id, user.role);

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("user_id", user.id);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } catch (AuthRepository.AuthException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void resetPasswordAfterOtp() {
        try {
            AuthRepository authRepository = new AuthRepository(this);
            authRepository.forgotPassword(email, newPassword, confirmPassword);

            Toast.makeText(this, "Đặt lại mật khẩu thành công", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } catch (AuthRepository.AuthException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String generateOtp() {
        int otp = 100000 + new Random().nextInt(900000);
        return String.valueOf(otp);
    }
}