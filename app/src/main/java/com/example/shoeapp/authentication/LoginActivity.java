package com.example.shoeapp.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shoeapp.R;
import com.example.shoeapp.admin.AdminDashboardActivity;
import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.entity.User;
import com.example.shoeapp.user.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private View passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailInput       = findViewById(R.id.emailInput);
        passwordInput    = findViewById(R.id.passwordInput);
        passwordField    = findViewById(R.id.passwordField);
        ImageButton passwordVisibility = findViewById(R.id.passwordVisibility);

        // Toggle hiện/ẩn mật khẩu
        passwordInput.setOnFocusChangeListener((view, focused) ->
                passwordField.setBackgroundResource(
                        focused ? R.drawable.bg_input_focused : R.drawable.bg_input_light));

        passwordVisibility.setOnClickListener(view -> {
            boolean hidden = passwordInput.getTransformationMethod()
                    instanceof PasswordTransformationMethod;
            if (hidden) {
                passwordInput.setTransformationMethod(null);
                passwordVisibility.setContentDescription(getString(R.string.auth_hide_password));
            } else {
                passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passwordVisibility.setContentDescription(getString(R.string.auth_show_password));
            }
            passwordInput.setSelection(passwordInput.length());
        });

        // Nút Sign In
        findViewById(R.id.signInButton).setOnClickListener(v -> handleLogin());

        // Đến trang Sign Up
        findViewById(R.id.signUp).setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class)));

        // Quên mật khẩu
        findViewById(R.id.forgotPassword).setOnClickListener(v ->
                startActivity(new Intent(this, ChangePasswordActivity.class)));
    }

    private void handleLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        try {
            AuthRepository authRepository = new AuthRepository(this);
            User user = authRepository.login(email, password);

            Toast.makeText(this, "Xin chào, " + user.fullName + "!", Toast.LENGTH_SHORT).show();
            SessionManager.saveSession(this, user.id, user.role);

            Intent intent;
            if ("ADMIN".equals(user.role)) {
                intent = new Intent(this, AdminDashboardActivity.class);
            } else {
                intent = new Intent(this, MainActivity.class);
            }

            intent.putExtra("user_id", user.id);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } catch (AuthRepository.AuthException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
