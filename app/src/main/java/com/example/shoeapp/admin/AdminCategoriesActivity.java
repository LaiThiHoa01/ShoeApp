package com.example.shoeapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import com.example.shoeapp.admin.adapter.AdminCategoryAdapter;
import com.example.shoeapp.user.CatalogActivity;
import com.example.shoeapp.model.Category;
import com.example.shoeapp.R;
import com.example.shoeapp.data.AppDatabase;
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

    private RecyclerView         recyclerView;
    private TextView             subtitle;
    private BottomNavigationView bottomNav;

    private AdminCategoryAdapter adapter;
    private List<Category>       categories;
    private AppDatabase          db;

    private static final int[] BG_COLORS = {
            R.color.orange_tint_15,
            R.color.stat_orders_bg,
            R.color.status_warning_bg,
            R.color.bg_dark_surface_4,
            R.color.stat_customers_bg,
            R.color.stat_products_bg
    };
    private static final int[] ACCENT_COLORS = {
            R.color.cat_sneakers,
            R.color.cat_running,
            R.color.status_warning,
            R.color.cat_casual,
            R.color.cat_basketball,
            R.color.cat_training
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_categories);
        db = AppDatabase.getDatabase(this);
        setupEdgeToEdge();
        bindViews();
        loadFromDb();
        setupRecyclerView();
        setupBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFromDb();
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
        recyclerView = findViewById(R.id.admin_categories_recycler);
        subtitle     = findViewById(R.id.admin_categories_subtitle);
        bottomNav    = findViewById(R.id.admin_bottom_nav);
        findViewById(R.id.admin_categories_btn_add)
                .setOnClickListener(v -> showAddCategoryDialog());
    }

    private void loadFromDb() {
        List<com.example.shoeapp.data.entity.Category> dbList =
                db.categoryDao().getAllCategories();

        categories = new ArrayList<>();
        int maxProducts = 0;

        List<Integer> counts = new ArrayList<>();
        for (com.example.shoeapp.data.entity.Category entity : dbList) {
            int count = db.productDao().getProductsByCategory(entity.id).size();
            counts.add(count);
            if (count > maxProducts) maxProducts = count;
        }
        if (maxProducts == 0) maxProducts = 1;

        for (int i = 0; i < dbList.size(); i++) {
            com.example.shoeapp.data.entity.Category entity = dbList.get(i);
            int colorIdx = i % BG_COLORS.length;
            categories.add(new Category(
                    entity.id,
                    entity.name,
                    R.drawable.ic_shoe,
                    BG_COLORS[colorIdx],
                    ACCENT_COLORS[colorIdx],
                    counts.get(i),
                    maxProducts
            ));
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        updateSubtitle();
    }

    private void setupRecyclerView() {
        adapter = new AdminCategoryAdapter(this, categories, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        int gapPx = (int) (12 * getResources().getDisplayMetrics().density);
        recyclerView.addItemDecoration(new AdminProductsActivity.SpaceItemDecoration(gapPx));
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_categories);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
                overridePendingTransition(0, 0); finish(); return true;
            } else if (id == R.id.nav_users) {
                startActivity(new Intent(this, UserManagementActivity.class));
                overridePendingTransition(0, 0); finish(); return true;
            } else if (id == R.id.nav_products) {
                startActivity(new Intent(this, AdminProductsActivity.class));
                overridePendingTransition(0, 0); finish(); return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, AdminOrderManagementActivity.class));
                overridePendingTransition(0, 0); finish(); return true;
            }
            return id == R.id.nav_categories;
        });
    }

    private void updateSubtitle() {
        int totalProducts = 0;
        for (Category c : categories) totalProducts += c.getProductCount();
        subtitle.setText(String.format(Locale.getDefault(),
                "%d categories · %d products total",
                categories.size(), totalProducts));
    }

    private void showAddCategoryDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Tên danh mục");

        new AlertDialog.Builder(this)
                .setTitle("Thêm danh mục")
                .setView(input)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Tên không được trống", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    com.example.shoeapp.data.entity.Category entity =
                            new com.example.shoeapp.data.entity.Category();
                    entity.name      = name;
                    entity.isActive  = true;
                    entity.sortOrder = categories.size() + 1;
                    entity.createdAt = new java.text.SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            .format(new java.util.Date());
                    db.categoryDao().insert(entity);
                    loadFromDb();
                    Toast.makeText(this, "Đã thêm \"" + name + "\"", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onEditClick(Category category, int position) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(category.getName());

        new AlertDialog.Builder(this)
                .setTitle("Sửa danh mục")
                .setView(input)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) return;
                    List<com.example.shoeapp.data.entity.Category> all =
                            db.categoryDao().getAllCategories();
                    for (com.example.shoeapp.data.entity.Category e : all) {
                        if (e.id == category.getId()) {
                            e.name = newName;
                            db.categoryDao().update(e);
                            break;
                        }
                    }
                    loadFromDb();
                    Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDeleteClick(Category category, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa danh mục")
                .setMessage("Bạn có chắc muốn xóa \"" + category.getName() + "\" không?\n"
                        + "Thao tác này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    List<com.example.shoeapp.data.entity.Category> all =
                            db.categoryDao().getAllCategories();
                    for (com.example.shoeapp.data.entity.Category e : all) {
                        if (e.id == category.getId()) {
                            db.categoryDao().delete(e);
                            break;
                        }
                    }
                    loadFromDb();
                    Toast.makeText(this, "Đã xóa \"" + category.getName() + "\"",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onViewAllClick(Category category, int position) {
        Intent intent = new Intent(this, CatalogActivity.class);
        intent.putExtra("filter_category", category.getName());
        startActivity(intent);
    }
}
