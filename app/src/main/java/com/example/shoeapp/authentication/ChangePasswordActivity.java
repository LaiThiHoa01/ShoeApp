package com.example.shoeapp.authentication;

import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shoeapp.R;

public class ChangePasswordActivity extends AppCompatActivity {

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

        findViewById(R.id.backButton).setOnClickListener(view -> finish());
        setupPasswordToggle(R.id.currentPasswordInput, R.id.currentPasswordVisibility);
        setupPasswordToggle(R.id.newPasswordInput, R.id.newPasswordVisibility);
        setupPasswordToggle(R.id.confirmPasswordInput, R.id.confirmPasswordVisibility);
        findViewById(R.id.updatePasswordButton).setOnClickListener(view -> finish());
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
}
