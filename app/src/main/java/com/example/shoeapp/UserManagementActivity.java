package com.example.shoeapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UserManagementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        if (getWindow() != null) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.bg_dark_primary));
        }

        // Thiết lập Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_users);
        
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return id == R.id.nav_users;
        });

        // Xử lý sự kiện bấm nút "+ Add User" để hiện Bottom Sheet
        findViewById(R.id.btn_add_user).setOnClickListener(v -> {
            AddUserBottomSheet bottomSheet = new AddUserBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), "AddUserBottomSheet");
        });
    }
}
