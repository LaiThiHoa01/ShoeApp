package com.example.shoeapp.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shoeapp.R;

public class LoginActivity extends AppCompatActivity {

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_main);
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//
//        EditText passwordInput = findViewById(R.id.passwordInput);
//        View passwordField = findViewById(R.id.passwordField);
//        ImageButton passwordVisibility = findViewById(R.id.passwordVisibility);
//
//        passwordInput.setOnFocusChangeListener((view, focused) ->
//                passwordField.setBackgroundResource(
//                        focused ? R.drawable.bg_input_focused : R.drawable.bg_input_light));
//
//        passwordVisibility.setOnClickListener(view -> {
//            boolean passwordHidden =
//                    passwordInput.getTransformationMethod() instanceof PasswordTransformationMethod;
//            if (passwordHidden) {
//                passwordInput.setTransformationMethod(null);
//                passwordVisibility.setContentDescription(getString(R.string.auth_hide_password));
//            } else {
//                passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
//                passwordVisibility.setContentDescription(getString(R.string.auth_show_password));
//            }
//            passwordInput.setSelection(passwordInput.length());
//        });
//
//        findViewById(R.id.signInButton).setOnClickListener(view -> {
//            startActivity(new Intent(this, MainActivity.class));
//            finish();
//        });
//        findViewById(R.id.signUp).setOnClickListener(view ->
//                startActivity(new Intent(this, SignUpActivity.class)));
//        findViewById(R.id.forgotPassword).setOnClickListener(view ->
//                startActivity(new Intent(this, ChangePasswordActivity.class)));
//    }
}
