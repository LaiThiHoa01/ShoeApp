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
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shoeapp.R;
import com.example.shoeapp.admin.AdminDashboardActivity;
import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.entity.User;
import com.example.shoeapp.user.MainActivity;

import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private View passwordField;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

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

        findViewById(R.id.signInButton).setOnClickListener(v -> handleLogin());

        findViewById(R.id.signUp).setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class)));

        // Quên mật khẩu
        findViewById(R.id.forgotPassword).setOnClickListener(v ->
                startActivity(new Intent(this, ChangePasswordActivity.class)));

        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getData() == null) {
                        Toast.makeText(this, "Bạn đã hủy đăng nhập Google", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Task<GoogleSignInAccount> task =
                            GoogleSignIn.getSignedInAccountFromIntent(result.getData());

                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        String idToken = account.getIdToken();

                        if (idToken == null) {
                            Toast.makeText(this, "Không lấy được Google ID Token", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        firebaseAuthWithGoogle(idToken);

                    } catch (ApiException e) {
                        Toast.makeText(
                                this,
                                "Đăng nhập Google thất bại: " + e.getStatusCode(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
        findViewById(R.id.googleSignInButton).setOnClickListener(v -> signInWithGoogle());
    }

    private void handleLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        try {
            AuthRepository authRepository = new AuthRepository(this);
            User user = authRepository.login(email, password);

            Toast.makeText(this, "Xin chào, " + user.fullName + "!", Toast.LENGTH_SHORT).show();
            SessionManager.saveSession(this, user.id, user.role);
            openHomeByRole(user);

        } catch (AuthRepository.AuthException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    if (firebaseUser == null) {
                        Toast.makeText(this, "Không lấy được tài khoản Google", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String uid = firebaseUser.getUid();
                    String email = firebaseUser.getEmail();
                    String name = firebaseUser.getDisplayName();
                    String avatar = firebaseUser.getPhotoUrl() != null
                            ? firebaseUser.getPhotoUrl().toString()
                            : "";

                    try {
                        AuthRepository authRepository = new AuthRepository(this);
                        User user = authRepository.loginWithGoogle(uid, email, name, avatar);

                        SessionManager.saveSession(this, user.id, user.role);
                        openHomeByRole(user);

                    } catch (AuthRepository.AuthException e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Firebase Auth lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void openHomeByRole(User user) {
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
    }
}
