package com.example.shoeapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shoeapp.ui.AddUserBottomSheet;
import com.example.shoeapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UserManagementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        // EdgeToEdge — đồng nhất với các admin screens khác
        androidx.activity.EdgeToEdge.enable(this);
        View root = findViewById(R.id.admin_user_management_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        BottomNavigationView bottomNav = findViewById(R.id.admin_bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_users);
        
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_categories) {
                startActivity(new Intent(this, AdminCategoriesActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_products) {
                startActivity(new Intent(this, AdminProductsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, AdminOrderManagementActivity.class));
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
