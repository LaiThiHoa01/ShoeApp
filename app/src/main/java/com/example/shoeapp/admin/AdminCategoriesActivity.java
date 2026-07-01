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

public class AdminCategoriesActivity extends BaseAdminActivity
        implements AdminCategoryAdapter.OnCategoryActionListener {

    private RecyclerView         recyclerView;
    private TextView             subtitle;
    private BottomNavigationView bottomNav;

    private AdminCategoryAdapter adapter;
    private List<Category>       categories = new ArrayList<>();
    private AppDatabase          db;

    private static final int[] BG_COLORS = {
            R.color.orange_tint_15,
            R.color.stat_orders_bg,
            R.color.status_warning_bg,
            R.color.bg_input,
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
        androidx.activity.EdgeToEdge.enable(this);
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
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_categories);
        }
        loadFromDb();
    }

    private void setupEdgeToEdge() {
        View root = findViewById(R.id.admin_categories_root);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return WindowInsetsCompat.CONSUMED;
            });
        }
    }

    private void bindViews() {
        recyclerView = findViewById(R.id.admin_categories_recycler);
        subtitle     = findViewById(R.id.admin_categories_subtitle);
        bottomNav    = findViewById(R.id.admin_bottom_nav);
        findViewById(R.id.admin_categories_btn_add)
                .setOnClickListener(v -> showAddCategoryDialog());
    }

    private void loadFromDb() {
        new Thread(() -> {
            List<com.example.shoeapp.data.entity.Category> dbList =
                    db.categoryDao().getAllCategories();
            List<Category> tempCategories = new ArrayList<>();
            int maxProductsVal = 0;

            if (dbList != null) {
                boolean hasUpdate = false;
                for (com.example.shoeapp.data.entity.Category entity : dbList) {
                    if (entity.iconUrl != null && entity.iconUrl.trim().endsWith(".avif")) {
                        entity.iconUrl = "https://res.cloudinary.com/dnmowplwi/image/upload/v1768911723/AIR_JORDAN_1_LOW_nocz0l.jpg";
                        db.categoryDao().update(entity);
                        hasUpdate = true;
                    }
                }
                if (hasUpdate) {
                    dbList = db.categoryDao().getAllCategories();
                }

                List<Integer> counts = new ArrayList<>();
                for (com.example.shoeapp.data.entity.Category entity : dbList) {
                    int count = db.productDao().getProductsByCategory(entity.id).size();
                    counts.add(count);
                    if (count > maxProductsVal) maxProductsVal = count;
                }
                if (maxProductsVal == 0) maxProductsVal = 1;

                for (int i = 0; i < dbList.size(); i++) {
                    com.example.shoeapp.data.entity.Category entity = dbList.get(i);
                    int colorIdx = i % BG_COLORS.length;
                    Category model = new Category(
                            entity.id,
                            entity.name,
                            R.drawable.ic_shoe,
                            BG_COLORS[colorIdx],
                            ACCENT_COLORS[colorIdx],
                            counts.get(i),
                            maxProductsVal,
                            entity.isActive
                    );
                    model.setIconUrl(entity.iconUrl);
                    tempCategories.add(model);
                }
            }

            runOnUiThread(() -> {
                if (categories == null) {
                    categories = new ArrayList<>();
                }
                categories.clear();
                categories.addAll(tempCategories);

                if (adapter != null) {
                    adapter.submitList(new ArrayList<>(categories));
                }
                updateSubtitle();
            });
        }).start();
    }

    private void setupRecyclerView() {
        adapter = new AdminCategoryAdapter(this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.submitList(new ArrayList<>(categories));
        int gapPx = (int) (12 * getResources().getDisplayMetrics().density);
        recyclerView.addItemDecoration(new AdminProductsActivity.SpaceItemDecoration(gapPx));
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_categories);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;
            if (id == R.id.nav_dashboard) {
                intent = new Intent(this, AdminDashboardActivity.class);
            } else if (id == R.id.nav_users) {
                intent = new Intent(this, UserManagementActivity.class);
            } else if (id == R.id.nav_products) {
                intent = new Intent(this, AdminProductsActivity.class);
            } else if (id == R.id.nav_orders) {
                intent = new Intent(this, AdminOrderManagementActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }
            return id == R.id.nav_categories;
        });

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(AdminCategoriesActivity.this, AdminDashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });
    }

    private void updateSubtitle() {
        int totalProducts = 0;
        for (Category c : categories) totalProducts += c.getProductCount();
        subtitle.setText(String.format(Locale.getDefault(),
                "%d danh mục · %d sản phẩm",
                categories.size(), totalProducts));
    }

    private void showAddCategoryDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Tên danh mục");

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Thêm danh mục")
                .setView(input)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Tên không được trống", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    List<com.example.shoeapp.data.entity.Category> existing =
                            db.categoryDao().getAllCategories();
                    for (com.example.shoeapp.data.entity.Category e : existing) {
                        if (e.name.equalsIgnoreCase(name)) {
                            Toast.makeText(this,
                                    "Danh mục \"" + name + "\" đã tồn tại",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
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

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
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
    public void onToggleActiveClick(Category category, boolean isActive, int position) {
        List<com.example.shoeapp.data.entity.Category> all =
                db.categoryDao().getAllCategories();
        for (com.example.shoeapp.data.entity.Category e : all) {
            if (e.id == category.getId()) {
                e.isActive = isActive;
                db.categoryDao().update(e);
                break;
            }
        }
        category.setActive(isActive);
        loadFromDb();
        String statusStr = isActive ? "bật hoạt động" : "vô hiệu hóa";
        Toast.makeText(this, "Đã " + statusStr + " danh mục \"" + category.getName() + "\"",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewAllClick(Category category, int position) {
        Intent intent = new Intent(this, CatalogActivity.class);
        intent.putExtra("filter_category", category.getName());
        startActivity(intent);
    }
}
