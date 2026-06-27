package com.example.shoeapp.authentication;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shoeapp.R;
import com.example.shoeapp.data.entity.User;
import com.example.shoeapp.user.MainActivity;

public class SignUpActivity extends AppCompatActivity {

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

        setupPasswordToggle();
        setupTermsText();

        findViewById(R.id.signInLink).setOnClickListener(view -> finish());
        findViewById(R.id.bottomSignInLink).setOnClickListener(view -> finish());
        findViewById(R.id.continueButton).setOnClickListener(view -> handleSignUp());
    }

    private void handleSignUp() {
        EditText firstNameInput = findViewById(R.id.firstNameInput);
        EditText lastNameInput = findViewById(R.id.lastNameInput);
        EditText emailInput = findViewById(R.id.emailInput);
        EditText phoneInput = findViewById(R.id.phoneInput);
        EditText passwordInput = findViewById(R.id.passwordInput);

        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        try {
            AuthRepository authRepository = new AuthRepository(this);
            User user = authRepository.register(firstName, lastName, email, phone, password);

            Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
            SessionManager.saveUserId(this, user.id);

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("user_id", user.id);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } catch (AuthRepository.AuthException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupPasswordToggle() {
        EditText passwordInput = findViewById(R.id.passwordInput);
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

    private void setupTermsText() {
        TextView termsText = findViewById(R.id.termsText);
        String text = getString(R.string.auth_terms_notice);
        SpannableString spannable = new SpannableString(text);
        int accent = ContextCompat.getColor(this, R.color.brand_orange);

        colorRange(spannable, text, getString(R.string.auth_terms), accent);
        colorRange(spannable, text, getString(R.string.auth_privacy), accent);
        termsText.setText(spannable);
    }

    private static void colorRange(SpannableString spannable, String fullText, String target, int color) {
        int start = fullText.indexOf(target);
        if (start < 0) {
            return;
        }
        spannable.setSpan(
                new ForegroundColorSpan(color),
                start,
                start + target.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

}
