package com.example.shoeapp.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shoeapp.R;

public class SignUpActivity extends AppCompatActivity {
    private EditText firstNameInput, lastNameInput, emailInput, phoneInput, passwordInput;
    private LinearLayout strengthBarContainer;
    private TextView strengthText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindViews();
        setupPasswordToggle();
        setupPasswordStrength();

        findViewById(R.id.signInLink).setOnClickListener(view -> finish());
        findViewById(R.id.bottomSignInLink).setOnClickListener(view -> finish());
        findViewById(R.id.continueButton).setOnClickListener(view -> handleSignUp());
    }

    private void bindViews() {
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        strengthBarContainer = findViewById(R.id.strengthBarContainer);
        strengthText = findViewById(R.id.strengthText);
    }

    private void handleSignUp() {
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (firstName.isEmpty()) { firstNameInput.setError("Vui lòng nhập tên"); return; }
        if (lastName.isEmpty()) { lastNameInput.setError("Vui lòng nhập họ"); return; }
        if (email.isEmpty()) { emailInput.setError("Vui lòng nhập email"); return; }
        if (phone.isEmpty()) { phoneInput.getText().toString(); phoneInput.setError("Vui lòng nhập số điện thoại"); return; }

        if (password.isEmpty()) {
            passwordInput.setError("Vui lòng nhập mật khẩu");
            return;
        }

        if (!PasswordValidator.isStrong(password)) {
            Toast.makeText(
                    this,
                    "Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, chữ số và ký tự đặc biệt!",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        AuthRepository authRepository = new AuthRepository(this);
        if (authRepository.isEmailExists(email)) {
            emailInput.setError("Email đã được sử dụng");
            return;
        }

        Intent intent = new Intent(this, VerifyEmailOtpActivity.class);
        intent.putExtra(VerifyEmailOtpActivity.EXTRA_MODE, VerifyEmailOtpActivity.MODE_SIGN_UP);
        intent.putExtra(VerifyEmailOtpActivity.EXTRA_FIRST_NAME, firstName);
        intent.putExtra(VerifyEmailOtpActivity.EXTRA_LAST_NAME, lastName);
        intent.putExtra(VerifyEmailOtpActivity.EXTRA_EMAIL, email);
        intent.putExtra(VerifyEmailOtpActivity.EXTRA_PHONE, phone);
        intent.putExtra(VerifyEmailOtpActivity.EXTRA_PASSWORD, password);
        startActivity(intent);
    }

    private void setupPasswordToggle() {
        View passwordField = findViewById(R.id.passwordField);
        ImageButton passwordVisibility = findViewById(R.id.passwordVisibility);

        passwordInput.setOnFocusChangeListener((view, focused) ->
                passwordField.setBackgroundResource(
                        focused ? R.drawable.bg_input_focused : R.drawable.bg_signup_field));

        passwordVisibility.setOnClickListener(view -> {
            boolean passwordHidden =
                    passwordInput.getTransformationMethod() instanceof PasswordTransformationMethod;

            if (passwordHidden) {
                passwordInput.setTransformationMethod(null);
                passwordVisibility.setContentDescription(getString(R.string.auth_hide_password));
            } else {
                passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passwordVisibility.setContentDescription(getString(R.string.auth_show_password));
            }

            passwordInput.setSelection(passwordInput.length());
        });
    }

    private void setupPasswordStrength() {
        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString();

                if (password.isEmpty()) {
                    strengthBarContainer.setVisibility(View.GONE);
                    strengthText.setVisibility(View.GONE);
                    return;
                }

                strengthBarContainer.setVisibility(View.VISIBLE);
                strengthText.setVisibility(View.VISIBLE);

                int strength = PasswordValidator.getStrength(password);
                updateStrengthBars(strength);

                String[] levels = {"Rất yếu", "Yếu", "Trung bình", "Khá", "Mạnh"};
                strengthText.setText(levels[strength]);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void updateStrengthBars(int score) {
        int[] colors = {
                android.R.color.darker_gray,
                android.R.color.holo_red_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_orange_light,
                R.color.brand_orange
        };

        for (int i = 0; i < strengthBarContainer.getChildCount(); i++) {
            View bar = strengthBarContainer.getChildAt(i);
            int colorRes = i < score ? colors[score] : colors[0];
            bar.setBackgroundColor(ContextCompat.getColor(SignUpActivity.this, colorRes));
        }
    }
}