package com.example.shoeapp.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shoeapp.R;
import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.entity.DeliveryAddress;
import com.example.shoeapp.data.entity.User;
import com.example.shoeapp.ui.BaseSoleStepActivity;
import com.example.shoeapp.ui.BottomNavHelper;
import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends BaseSoleStepActivity {
    private TextView emailText;
    private TextView avatarText;
    private AppDatabase db;
    private User currentUser;
    private TextView nameText;
    private TextView addressText;
    private TextView phoneText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setupScreen(BottomNavHelper.TAG_PROFILE);

        db = AppDatabase.getDatabase(this);
        bindViews();
        loadUser();

        MaterialButton manageAddressesButton = findViewById(R.id.profile_manage_addresses_button);
        manageAddressesButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AddressBookActivity.class);
            startActivity(intent);
        });
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
        currentUser = db.userDao().getUserById(ClientCartRepository.DEMO_USER_ID);

        if (currentUser == null) {
            currentUser = new User();
            currentUser.id = ClientCartRepository.DEMO_USER_ID;
            currentUser.email = "khachhang@solestep.vn";
            currentUser.fullName = "Khách hàng";
            currentUser.phoneNumber = "";
            currentUser.role = "USER";
            currentUser.isActive = true;
            currentUser.createdAt = "2026-06-26";
            currentUser.userId = "USR-DEMO-CLIENT";
            db.userDao().insert(currentUser);
            currentUser = db.userDao().getUserByEmail(currentUser.email);
        }

        emailText.setText("Email: "+currentUser.email);
        nameText.setText("Họ tên: "+currentUser.fullName);
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
}