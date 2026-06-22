package com.example.shoeapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.example.shoeapp.admin.adapter.AdminProductAdapter;
import com.example.shoeapp.model.Product;
import com.example.shoeapp.R;
import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.entity.Brand;
import com.example.shoeapp.data.entity.Category;
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
import java.util.Collections;
import java.util.List;

public class AdminProductsActivity extends AppCompatActivity
        implements AdminProductAdapter.OnProductActionListener {

    private RecyclerView         recyclerView;
    private EditText             searchInput;
    private TextView             filterAll, filterSneakers, filterRunning, filterCasual;
    private BottomNavigationView bottomNav;

    private AdminProductAdapter  adapter;
    private List<Product>        allProducts;
    private List<Product>        filteredProducts;
    private AppDatabase          db;

    private List<com.example.shoeapp.data.entity.Product> dbProducts = new ArrayList<>();

    private String currentCategory = "All";
    private String currentSearch   = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_products);
        db = AppDatabase.getDatabase(this);
        setupEdgeToEdge();
        bindViews();
        loadFromDb();
        setupRecyclerView();
        setupSearch();
        setupFilterChips();
        setupBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFromDb();
    }

    private void setupEdgeToEdge() {
        androidx.activity.EdgeToEdge.enable(this);
        View root = findViewById(R.id.admin_products_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void bindViews() {
        recyclerView   = findViewById(R.id.admin_products_recycler);
        searchInput    = findViewById(R.id.admin_products_search_input);
        filterAll      = findViewById(R.id.admin_filter_all);
        filterSneakers = findViewById(R.id.admin_filter_sneakers);
        filterRunning  = findViewById(R.id.admin_filter_running);
        filterCasual   = findViewById(R.id.admin_filter_casual);
        bottomNav      = findViewById(R.id.admin_bottom_nav);
        findViewById(R.id.admin_products_btn_add)
                .setOnClickListener(v -> onAddProductClick());
    }

    private void loadFromDb() {
        dbProducts  = db.productDao().getAllProducts();
        allProducts = new ArrayList<>();

        for (com.example.shoeapp.data.entity.Product entity : dbProducts) {
            Brand brand = db.productDao().getBrandById(entity.brandId);
            String brandName = brand != null ? brand.name : "Unknown Brand";

            String categoryName = "Unknown";
            List<Category> cats = db.categoryDao().getAllCategories();
            for (Category c : cats) {
                if (c.id == entity.shoeCategory) {
                    categoryName = c.name;
                    break;
                }
            }

            List<com.example.shoeapp.data.entity.ProductVariant> variants =
                    db.productDao().getVariantsByProduct(entity.id);
            int totalStock = 0;
            for (com.example.shoeapp.data.entity.ProductVariant v : variants) {
                totalStock += v.stock;
            }

            float rating     = db.productDao().getAverageRating(entity.id);
            int reviewCount  = db.productDao().getReviewsByProduct(entity.id).size();

            allProducts.add(new Product(
                    entity.id,
                    entity.name,
                    brandName + " · " + categoryName,
                    categoryName,
                    entity.price,
                    entity.originalPrice > 0 ? entity.originalPrice : entity.price,
                    totalStock,
                    entity.isAvailable,
                    Collections.emptyList(),
                    rating,
                    reviewCount,
                    R.drawable.ic_shoe
            ));
        }

        filteredProducts = new ArrayList<>(allProducts);
        if (adapter != null) applyFilters();
    }

    private void setupRecyclerView() {
        adapter = new AdminProductAdapter(this, filteredProducts, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        int gapPx = (int) (12 * getResources().getDisplayMetrics().density);
        recyclerView.addItemDecoration(new SpaceItemDecoration(gapPx));
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                currentSearch = s.toString().trim().toLowerCase();
                applyFilters();
            }
        });
    }

    private void setupFilterChips() {
        filterAll.setOnClickListener(v      -> selectCategory("All",      filterAll));
        filterSneakers.setOnClickListener(v -> selectCategory("Sneakers", filterSneakers));
        filterRunning.setOnClickListener(v  -> selectCategory("Running",  filterRunning));
        filterCasual.setOnClickListener(v   -> selectCategory("Casual",   filterCasual));
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_products);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
                overridePendingTransition(0, 0); finish(); return true;
            } else if (id == R.id.nav_users) {
                startActivity(new Intent(this, UserManagementActivity.class));
                overridePendingTransition(0, 0); finish(); return true;
            } else if (id == R.id.nav_categories) {
                startActivity(new Intent(this, AdminCategoriesActivity.class));
                overridePendingTransition(0, 0); finish(); return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, AdminOrderManagementActivity.class));
                overridePendingTransition(0, 0); finish(); return true;
            }
            return id == R.id.nav_products;
        });
    }

    private void selectCategory(String category, TextView selectedChip) {
        currentCategory = category;
        TextView[] chips = { filterAll, filterSneakers, filterRunning, filterCasual };
        for (TextView chip : chips) {
            chip.setBackgroundResource(R.drawable.bg_admin_chip);
            chip.setTextColor(getColor(R.color.text_dark_tertiary));
        }
        selectedChip.setBackgroundResource(R.drawable.bg_admin_chip_selected);
        selectedChip.setTextColor(getColor(R.color.brand_white));
        applyFilters();
    }

    private void applyFilters() {
        filteredProducts.clear();
        for (Product p : allProducts) {
            boolean matchCategory = currentCategory.equals("All")
                    || p.getCategory().equals(currentCategory);
            boolean matchSearch = currentSearch.isEmpty()
                    || p.getName().toLowerCase().contains(currentSearch)
                    || p.getBrand().toLowerCase().contains(currentSearch);
            if (matchCategory && matchSearch) filteredProducts.add(p);
        }
        adapter.notifyDataSetChanged();
    }

    private void onAddProductClick() {
        Toast.makeText(this, "Tính năng thêm sản phẩm đang được phát triển", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditClick(Product product, int position) {
        Toast.makeText(this, "Sửa: " + product.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(Product product, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc muốn xóa \"" + product.getName() + "\" không?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteProduct(product))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteProduct(Product product) {
        for (com.example.shoeapp.data.entity.Product entity : dbProducts) {
            if (entity.id == product.getId()) {
                db.productDao().delete(entity);
                break;
            }
        }
        loadFromDb();
        Toast.makeText(this, "Đã xóa \"" + product.getName() + "\"", Toast.LENGTH_SHORT).show();
    }

    static class SpaceItemDecoration extends RecyclerView.ItemDecoration {
        private final int space;
        SpaceItemDecoration(int space) { this.space = space; }

        @Override
        public void getItemOffsets(@androidx.annotation.NonNull android.graphics.Rect outRect,
                                   @androidx.annotation.NonNull View view,
                                   @androidx.annotation.NonNull RecyclerView parent,
                                   @androidx.annotation.NonNull RecyclerView.State state) {
            outRect.bottom = space;
        }
    }
}