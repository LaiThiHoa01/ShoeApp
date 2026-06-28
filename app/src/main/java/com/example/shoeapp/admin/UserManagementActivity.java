package com.example.shoeapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.R;
import com.example.shoeapp.admin.adapter.AdminUserAdapter;
import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.model.UserWithStats;
import com.example.shoeapp.ui.AddUserBottomSheet;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class UserManagementActivity extends AppCompatActivity implements AdminUserAdapter.OnUserActionListener {

    private AppDatabase db;
    private AdminUserAdapter adapter;
    private RecyclerView rvUsers;
    private EditText etSearch;
    private TextView tvTotal, tvActive, tvInactive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.activity.EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        db = AppDatabase.getDatabase(this);

        // Xử lý Insets vùng an toàn
        View root = findViewById(R.id.admin_user_management_root);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return WindowInsetsCompat.CONSUMED;
            });
        }

        // Setup Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.admin_bottom_nav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_users);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_users) return true;

                Intent intent = null;
                if (id == R.id.nav_dashboard) {
                    intent = new Intent(this, AdminDashboardActivity.class);
                } else if (id == R.id.nav_categories) {
                    intent = new Intent(this, AdminCategoriesActivity.class);
                } else if (id == R.id.nav_products) {
                    intent = new Intent(this, AdminProductsActivity.class);
                } else if (id == R.id.nav_orders) {
                    intent = new Intent(this, AdminOrderManagementActivity.class);
                }

                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                }
                return false;
            });
        }

        // Ánh xạ các View thống kê và tìm kiếm
        tvTotal = findViewById(R.id.tv_total_users);
        tvActive = findViewById(R.id.tv_active_users);
        tvInactive = findViewById(R.id.tv_inactive_users);
        etSearch = findViewById(R.id.et_search_users);
        rvUsers = findViewById(R.id.rv_users);

        // Cài đặt RecyclerView
        if (rvUsers != null) {
            rvUsers.setLayoutManager(new LinearLayoutManager(this));
            adapter = new AdminUserAdapter(this, this);
            rvUsers.setAdapter(adapter);
        }

        // Click thêm thành viên mới
        View btnAddUser = findViewById(R.id.btn_add_user);
        if (btnAddUser != null) {
            btnAddUser.setOnClickListener(v -> {
                AddUserBottomSheet bottomSheet = AddUserBottomSheet.newInstance(this::loadUsers);
                bottomSheet.show(getSupportFragmentManager(), "AddUserBottomSheet");
            });
        }

        // Lắng nghe sự kiện tìm kiếm
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchUsers(s.toString().trim());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }

    private void loadUsers() {
        new Thread(() -> {
            List<UserWithStats> users = db.userDao().getAllCustomersWithStats();
            updateUI(users);
        }).start();
    }

    private void searchUsers(String query) {
        if (query.isEmpty()) {
            loadUsers();
            return;
        }
        new Thread(() -> {
            List<UserWithStats> filtered = db.userDao().searchCustomersWithStats(query);
            updateUI(filtered);
        }).start();
    }

    private void updateUI(List<UserWithStats> users) {
        int total = users.size();
        int active = 0;
        int inactive = 0;

        for (UserWithStats item : users) {
            if (item.user.isActive) {
                active++;
            } else {
                inactive++;
            }
        }

        final int finalTotal = total;
        final int finalActive = active;
        final int finalInactive = inactive;

        runOnUiThread(() -> {
            if (tvTotal != null) tvTotal.setText(String.valueOf(finalTotal));
            if (tvActive != null) tvActive.setText(String.valueOf(finalActive));
            if (tvInactive != null) tvInactive.setText(String.valueOf(finalInactive));
            if (adapter != null) {
                adapter.submitList(new ArrayList<>(users));
            }
        });
    }

    @Override
    public void onEditClick(UserWithStats item) {
        AddUserBottomSheet bottomSheet = AddUserBottomSheet.newInstance(item.user.id, this::loadUsers);
        bottomSheet.show(getSupportFragmentManager(), "AddUserBottomSheet");
    }
}
