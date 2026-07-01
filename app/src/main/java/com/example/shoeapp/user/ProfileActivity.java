package com.example.shoeapp.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shoeapp.R;
import com.example.shoeapp.authentication.SessionManager;
import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.entity.DeliveryAddress;
import com.example.shoeapp.data.entity.User;
import com.example.shoeapp.data.repo.UserRepository;
import com.example.shoeapp.ui.BaseSoleStepActivity;
import com.example.shoeapp.ui.BottomNavHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButton;
import com.example.shoeapp.admin.AdminDashboardActivity;
import com.example.shoeapp.authentication.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends BaseSoleStepActivity {
    private TextView emailText;
    private TextView avatarText;
    private AppDatabase db;
    private User currentUser;
    private TextView nameText;
    private TextView addressText;
    private TextView phoneText;
    private UserRepository userRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setupScreen(BottomNavHelper.TAG_PROFILE);
        userRepository = new UserRepository(this);

        db = AppDatabase.getDatabase(this);
        bindViews();
        loadUser();

        MaterialButton manageAddressesButton = findViewById(R.id.profile_manage_addresses_button);
        manageAddressesButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AddressBookActivity.class);
            startActivity(intent);
        });

        MaterialButton adminPageButton = findViewById(R.id.btn_admin_page);
        MaterialButton logoutButton = findViewById(R.id.btn_logout);

        if (SessionManager.isAdmin(this)) {
            adminPageButton.setVisibility(View.VISIBLE);
        } else {
            adminPageButton.setVisibility(View.GONE);
        }

        adminPageButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> handleLogout());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDefaultAddress();
    }
    private void bindViews() {
        emailText = findViewById(R.id.profile_email_text);
        avatarText = findViewById(R.id.profile_avatar_text);
        nameText = findViewById(R.id.profile_name_text);
        addressText = findViewById(R.id.profile_address_text);
        phoneText = findViewById(R.id.profile_phone_text);
    }
    private void loadUser() {
        currentUser = userRepository.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        emailText.setText("Email: " + currentUser.email);
        nameText.setText("Họ tên: " + currentUser.fullName);
        avatarText.setText(getInitial(currentUser.fullName));

        loadDefaultAddress();
    }
    private void loadDefaultAddress() {
        if (currentUser == null) return;

        DeliveryAddress defaultAddress = db.addressDao().getDefaultAddress(currentUser.userId);

        if (defaultAddress != null) {
            addressText.setText("Địa chỉ mặc định: " + defaultAddress.address);
            addressText.setVisibility(View.VISIBLE);

            if (defaultAddress.phoneNumber != null && !defaultAddress.phoneNumber.trim().isEmpty()) {
                phoneText.setText("SĐT: " + defaultAddress.phoneNumber);
                phoneText.setVisibility(View.VISIBLE);
            } else {
                phoneText.setVisibility(View.GONE);
            }
        } else {
            addressText.setText("Chưa thiết lập địa chỉ mặc định");
            addressText.setVisibility(View.VISIBLE);
            phoneText.setVisibility(View.GONE);
        }
    }
    private String getInitial(String name) {
        if (android.text.TextUtils.isEmpty(name)) {
            return "S";
        }
        return name.trim().substring(0, 1).toUpperCase();
    }
    private void handleLogout() {
        SessionManager.clear(ProfileActivity.this);

        com.google.firebase.auth.FirebaseAuth.getInstance().signOut();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignIn.getClient(ProfileActivity.this, gso).signOut();

        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}