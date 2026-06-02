package com.example.shoeapp.authentication;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shoeapp.R;

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
