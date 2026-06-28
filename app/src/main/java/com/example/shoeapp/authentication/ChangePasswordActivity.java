package com.example.shoeapp.authentication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shoeapp.R;

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText emailInput;
    private EditText newPasswordInput;
    private EditText confirmPasswordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailInput = findViewById(R.id.emailInput);
        newPasswordInput = findViewById(R.id.newPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        View confirmPasswordField = findViewById(R.id.confirmPasswordField);

        findViewById(R.id.backButton).setOnClickListener(view -> finish());

        setupPasswordToggle(R.id.newPasswordInput, R.id.newPasswordVisibility);
        setupPasswordToggle(R.id.confirmPasswordInput, R.id.confirmPasswordVisibility);

        findViewById(R.id.updatePasswordButton).setOnClickListener(view -> handleForgotPassword());


        TextView checkMarkIcon = findViewById(R.id.checkMarkIcon);
        TextView passwordMatchText = findViewById(R.id.passwordMatchText);

        TextWatcher passwordWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newPass = newPasswordInput.getText().toString();
                String confirmPass = confirmPasswordInput.getText().toString();

                if (!newPass.isEmpty() && newPass.equals(confirmPass)) {
                    checkMarkIcon.setVisibility(View.VISIBLE);
                    passwordMatchText.setVisibility(View.VISIBLE);
                    confirmPasswordField.setBackgroundResource(R.drawable.bg_change_password_valid);
                } else {
                    checkMarkIcon.setVisibility(View.GONE);
                    passwordMatchText.setVisibility(View.GONE);
                    confirmPasswordField.setBackgroundResource(R.drawable.bg_signup_field);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        newPasswordInput.addTextChangedListener(passwordWatcher);
        confirmPasswordInput.addTextChangedListener(passwordWatcher);
    }

    private void handleForgotPassword() {
        String email = emailInput.getText().toString().trim();
        String newPassword = newPasswordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (email.isEmpty()) { emailInput.setError("Vui lòng nhập email"); return; }
        if (newPassword.isEmpty()) { newPasswordInput.setError("Vui lòng nhập mật khẩu mới"); return; }
        if (confirmPassword.isEmpty()) { confirmPasswordInput.setError("Vui lòng xác nhận mật khẩu"); return; }
        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordInput.setError("Mật khẩu không khớp");
            return;
        }
        if (!isPasswordStrong(newPassword)) {
            Toast.makeText(this, "Mật khẩu mới chưa đủ mạnh!", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            AuthRepository authRepository = new AuthRepository(this);
            authRepository.forgotPassword(email, newPassword, confirmPassword);

            Toast.makeText(this, "Đặt lại mật khẩu thành công", Toast.LENGTH_SHORT).show();
            finish();

        } catch (AuthRepository.AuthException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupPasswordToggle(int inputId, int buttonId) {
        EditText passwordInput = findViewById(inputId);
        ImageButton visibilityButton = findViewById(buttonId);

        visibilityButton.setOnClickListener(view -> {
            boolean passwordHidden =
                    passwordInput.getTransformationMethod() instanceof PasswordTransformationMethod;

            if (passwordHidden) {
                passwordInput.setTransformationMethod(null);
                visibilityButton.setContentDescription(getString(R.string.auth_hide_password));
            } else {
                passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                visibilityButton.setContentDescription(getString(R.string.auth_show_password));
            }

            passwordInput.setSelection(passwordInput.length());
        });
    }

    private boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

        return hasUppercase && hasNumber && hasSpecial;
    }
}