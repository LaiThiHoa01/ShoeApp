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
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class UserManagementActivity extends AppCompatActivity implements AdminUserAdapter.OnUserActionListener {

    private AppDatabase db;
    private AdminUserAdapter adapter;
    private RecyclerView rvUsers;
    private EditText etSearch;
    private TextView tvTotal, tvActive, tvInactive;
    private String currentRoleFilter = "ALL";
    private String currentStatusFilter = "ALL";
    private TabLayout tabLayoutRoles;

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

        // Ánh xạ các View thống kê, tìm kiếm, nút lọc và TabLayout lọc vai trò
        tvTotal = findViewById(R.id.tv_total_users);
        tvActive = findViewById(R.id.tv_active_users);
        tvInactive = findViewById(R.id.tv_inactive_users);
        etSearch = findViewById(R.id.et_search_users);
        rvUsers = findViewById(R.id.rv_users);
        tabLayoutRoles = findViewById(R.id.tab_layout_roles);
        View btnFilter = findViewById(R.id.btn_filter_users);

        // Cài đặt RecyclerView
        if (rvUsers != null) {
            rvUsers.setLayoutManager(new LinearLayoutManager(this));
            adapter = new AdminUserAdapter(this, this);
            rvUsers.setAdapter(adapter);
        }

        // Lắng nghe sự kiện chuyển Tab của TabLayout lọc vai trò
        if (tabLayoutRoles != null) {
            tabLayoutRoles.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    int position = tab.getPosition();
                    if (position == 0) {
                        currentRoleFilter = "ALL";
                    } else if (position == 1) {
                        currentRoleFilter = "CUSTOMER";
                    } else if (position == 2) {
                        currentRoleFilter = "ADMIN";
                    }
                    refreshUsers();
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
        }

        // Lắng nghe sự kiện nhấn nút Lọc trên thanh tìm kiếm
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> showFilterDialog());
        }

        // Click thêm thành viên mới
        View btnAddUser = findViewById(R.id.btn_add_user);
        if (btnAddUser != null) {
            btnAddUser.setOnClickListener(v -> {
                AddUserBottomSheet bottomSheet = AddUserBottomSheet.newInstance(this::refreshUsers);
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
        refreshUsers();
    }

    private void refreshUsers() {
        if (etSearch != null) {
            searchUsers(etSearch.getText().toString().trim());
        } else {
            loadUsers();
        }
    }

    private void loadUsers() {
        new Thread(() -> {
            List<UserWithStats> users = db.userDao().getAllCustomersWithStats();
            List<UserWithStats> filtered = filterUsers(users);
            updateUI(filtered);
        }).start();
    }

    private void searchUsers(String query) {
        if (query.isEmpty()) {
            loadUsers();
            return;
        }
        new Thread(() -> {
            List<UserWithStats> filtered = db.userDao().searchCustomersWithStats(query);
            List<UserWithStats> filteredByRole = filterUsers(filtered);
            updateUI(filteredByRole);
        }).start();
    }

    private List<UserWithStats> filterUsers(List<UserWithStats> source) {
        List<UserWithStats> result = new ArrayList<>();
        for (UserWithStats item : source) {
            // Lọc vai trò
            boolean matchesRole = "ALL".equals(currentRoleFilter) || currentRoleFilter.equals(item.user.role);
            
            // Lọc trạng thái hoạt động
            boolean matchesStatus = "ALL".equals(currentStatusFilter) 
                    || ("ACTIVE".equals(currentStatusFilter) && item.user.isActive)
                    || ("INACTIVE".equals(currentStatusFilter) && !item.user.isActive);

            if (matchesRole && matchesStatus) {
                result.add(item);
            }
        }
        return result;
    }

    private void showFilterDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter_users, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        android.widget.RadioGroup rgRole = dialogView.findViewById(R.id.rg_filter_role);
        android.widget.RadioGroup rgStatus = dialogView.findViewById(R.id.rg_filter_status);
        View btnCancel = dialogView.findViewById(R.id.btn_cancel_filter);
        View btnApply = dialogView.findViewById(R.id.btn_apply_filter);

        if (rgRole != null) {
            if ("ALL".equals(currentRoleFilter)) {
                rgRole.check(R.id.rb_role_all);
            } else if ("CUSTOMER".equals(currentRoleFilter)) {
                rgRole.check(R.id.rb_role_customer);
            } else if ("ADMIN".equals(currentRoleFilter)) {
                rgRole.check(R.id.rb_role_admin);
            }
        }

        if (rgStatus != null) {
            if ("ALL".equals(currentStatusFilter)) {
                rgStatus.check(R.id.rb_status_all);
            } else if ("ACTIVE".equals(currentStatusFilter)) {
                rgStatus.check(R.id.rb_status_active);
            } else if ("INACTIVE".equals(currentStatusFilter)) {
                rgStatus.check(R.id.rb_status_inactive);
            }
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnApply != null) {
            btnApply.setOnClickListener(v -> {
                int checkedRole = rgRole != null ? rgRole.getCheckedRadioButtonId() : R.id.rb_role_all;
                if (checkedRole == R.id.rb_role_customer) {
                    currentRoleFilter = "CUSTOMER";
                } else if (checkedRole == R.id.rb_role_admin) {
                    currentRoleFilter = "ADMIN";
                } else {
                    currentRoleFilter = "ALL";
                }

                int checkedStatus = rgStatus != null ? rgStatus.getCheckedRadioButtonId() : R.id.rb_status_all;
                if (checkedStatus == R.id.rb_status_active) {
                    currentStatusFilter = "ACTIVE";
                } else if (checkedStatus == R.id.rb_status_inactive) {
                    currentStatusFilter = "INACTIVE";
                } else {
                    currentStatusFilter = "ALL";
                }

                int pos = 0;
                if ("CUSTOMER".equals(currentRoleFilter)) pos = 1;
                else if ("ADMIN".equals(currentRoleFilter)) pos = 2;

                if (tabLayoutRoles != null && tabLayoutRoles.getSelectedTabPosition() != pos) {
                    tabLayoutRoles.selectTab(tabLayoutRoles.getTabAt(pos));
                } else {
                    refreshUsers();
                }

                dialog.dismiss();
            });
        }

        dialog.show();
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
        AddUserBottomSheet bottomSheet = AddUserBottomSheet.newInstance(item.user.id, this::refreshUsers);
        bottomSheet.show(getSupportFragmentManager(), "AddUserBottomSheet");
    }
}
