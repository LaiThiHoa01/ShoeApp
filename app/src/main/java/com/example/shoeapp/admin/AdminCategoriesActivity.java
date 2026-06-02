package com.example.shoeapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.shoeapp.admin.adapter.AdminCategoryAdapter;
import com.example.shoeapp.user.CatalogActivity;
import com.example.shoeapp.model.Category;
import com.example.shoeapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class AdminCategoriesActivity extends AppCompatActivity
        implements AdminCategoryAdapter.OnCategoryActionListener {

    // ── Views ────────────────────────────────────────────────────────────────
    private RecyclerView         recyclerView;
    private TextView             subtitle;
    private BottomNavigationView bottomNav;

    // ── Data ─────────────────────────────────────────────────────────────────
    private AdminCategoryAdapter adapter;
    private List<Category>       categories;
    private static final int MAX_PRODUCTS_REFERENCE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_categories);
        setupEdgeToEdge();
        bindViews();
        setupData();
        setupRecyclerView();
        updateSubtitle();
        setupBottomNav();
    }

    private void setupEdgeToEdge() {
        androidx.activity.EdgeToEdge.enable(this);
        View root = findViewById(R.id.admin_categories_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void bindViews() {
        recyclerView   = findViewById(R.id.admin_categories_recycler);
        subtitle       = findViewById(R.id.admin_categories_subtitle);
        bottomNav      = findViewById(R.id.admin_bottom_nav);

        // Nút + Add
        findViewById(R.id.admin_categories_btn_add)
                .setOnClickListener(v -> onAddCategoryClick());
    }

    private void setupData() {
        categories = new ArrayList<>();

        categories.add(new Category(
                1, "Sneakers",
                R.drawable.ic_shoe,
                R.color.orange_tint_15,
                R.color.cat_sneakers,
                48, MAX_PRODUCTS_REFERENCE));

        categories.add(new Category(
                2, "Running",
                R.drawable.ic_shoe,
                R.color.stat_orders_bg,
                R.color.cat_running,
                32, MAX_PRODUCTS_REFERENCE));

        categories.add(new Category(
                3, "Boots",
                R.drawable.ic_shoe,
                R.color.status_warning_bg,
                R.color.status_warning,
                24, MAX_PRODUCTS_REFERENCE));

        categories.add(new Category(
                4, "Casual",
                R.drawable.ic_shoe,
                R.color.bg_dark_surface_4,
                R.color.cat_casual,
                36, MAX_PRODUCTS_REFERENCE));

        categories.add(new Category(
                5, "Basketball",
                R.drawable.ic_shoe,
                R.color.stat_customers_bg,
                R.color.cat_basketball,
                22, MAX_PRODUCTS_REFERENCE));

        categories.add(new Category(
                6, "Training",
                R.drawable.ic_shoe,
                R.color.stat_products_bg,
                R.color.cat_training,
                18, MAX_PRODUCTS_REFERENCE));
    }

    private void setupRecyclerView() {
        adapter = new AdminCategoryAdapter(this, categories, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Khoảng cách giữa các card
        int gapPx = (int) (12 * getResources().getDisplayMetrics().density);
        recyclerView.addItemDecoration(new AdminProductsActivity.SpaceItemDecoration(gapPx));
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_categories);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
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
            } else if (id == R.id.nav_users) {
                startActivity(new Intent(this, UserManagementActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return id == R.id.nav_categories;
        });
    }


    private void updateSubtitle() {
        int totalCategories = categories.size();
        int totalProducts   = 0;
        for (Category c : categories) {
            totalProducts += c.getProductCount();
        }
        subtitle.setText(String.format(Locale.getDefault(),
                "%d categories · %d products total",
                totalCategories, totalProducts));
    }


    private void onAddCategoryClick() {
        // TODO: startActivity(new Intent(this, AdminAddCategoryActivity.class));
        Toast.makeText(this, "Tính năng thêm danh mục đang được phát triển",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditClick(Category category, int position) {
        // TODO: startActivity(new Intent(this, AdminEditCategoryActivity.class)
        //           .putExtra("category_id", category.getId()));
        Toast.makeText(this,
                "Sửa danh mục: " + category.getName(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(Category category, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa danh mục")
                .setMessage("Bạn có chắc muốn xóa danh mục \""
                        + category.getName() + "\" không?\n"
                        + "Thao tác này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    categories.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, categories.size());
                    updateSubtitle();
                    Toast.makeText(this,
                            "Đã xóa \"" + category.getName() + "\"",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onViewAllClick(Category category, int position) {
        // Chuyển sang CatalogActivity, truyền filter category
        Intent intent = new Intent(this, CatalogActivity.class);
        intent.putExtra("filter_category", category.getName());
        startActivity(intent);
    }
}
