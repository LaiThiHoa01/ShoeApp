package com.example.shoeapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shoeapp.user.MainActivity;
import com.example.shoeapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // EdgeToEdge — đồng nhất với tất cả admin screens
        androidx.activity.EdgeToEdge.enable(this);
        View root = findViewById(R.id.admin_dashboard_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        BottomNavigationView bottomNav = findViewById(R.id.admin_bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_dashboard);

        // "View All" buttons
        findViewById(R.id.btn_view_all_orders).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminOrderManagementActivity.class));
            overridePendingTransition(0, 0);
        });

        findViewById(R.id.btn_view_all_products).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminProductsActivity.class));
            overridePendingTransition(0, 0);
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                // Đang ở Dashboard rồi, không làm gì hoặc refresh dữ liệu
                return true;
            } else if (id == R.id.nav_users) {
                startActivity(new Intent(this, UserManagementActivity.class));
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
            return false;
        });

        // Nút Logout hoặc quay về Client Home (Tùy chọn)
        findViewById(R.id.profile_image_bg).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
