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

import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminProductsActivity extends AppCompatActivity
        implements AdminProductAdapter.OnProductActionListener {

    private RecyclerView         recyclerView;
    private EditText             searchInput;
    private TextView             filterAll;
    private BottomNavigationView bottomNav;

    private AdminProductAdapter  adapter;
    private List<Product>        allProducts;
    private List<Product>        filteredProducts;
    private AppDatabase          db;

    private List<com.example.shoeapp.data.entity.Product> dbProducts = new ArrayList<>();
    private List<TextView> dynamicChips = new ArrayList<>();

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
        setupFilterChips();
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
        bottomNav      = findViewById(R.id.admin_bottom_nav);
        findViewById(R.id.admin_products_btn_add)
                .setOnClickListener(v -> onAddProductClick());
    }

    private void loadFromDb() {
        dbProducts  = db.productDao().getAllProducts();
        if (allProducts == null) {
            allProducts = new ArrayList<>();
        } else {
            allProducts.clear();
        }

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

            com.example.shoeapp.data.entity.ProductImg thumbnail =
                    db.productDao().getThumbnail(entity.id);
            String imageUrl = thumbnail != null ? thumbnail.imgUrl : null;

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
                    R.drawable.ic_shoe,
                    imageUrl
            ));
        }

        if (filteredProducts == null) {
            filteredProducts = new ArrayList<>(allProducts);
        } else {
            filteredProducts.clear();
            filteredProducts.addAll(allProducts);
        }
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
        LinearLayout filterRow = findViewById(R.id.admin_products_filter_row);
        if (filterRow.getChildCount() > 1) {
            filterRow.removeViews(1, filterRow.getChildCount() - 1);
        }
        dynamicChips.clear();
        dynamicChips.add(filterAll);

        filterAll.setOnClickListener(v -> selectCategory("All", filterAll));

        List<Category> dbCategories = db.categoryDao().getAllCategories();
        
        float density = getResources().getDisplayMetrics().density;
        int heightPx = (int) (32 * density);
        int marginStartPx = (int) (6 * density);
        int paddingHorizontalPx = (int) (14 * density);

        for (Category c : dbCategories) {
            TextView chip = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, heightPx);
            params.setMarginStart(marginStartPx);
            chip.setLayoutParams(params);
            
            chip.setGravity(android.view.Gravity.CENTER);
            chip.setPadding(paddingHorizontalPx, 0, paddingHorizontalPx, 0);
            
            // Đặt background dựa trên việc chip này có đang được chọn hay không
            if (currentCategory.equals(c.name)) {
                chip.setBackgroundResource(R.drawable.bg_admin_chip_selected);
                chip.setTextColor(getColor(R.color.brand_white));
            } else {
                chip.setBackgroundResource(R.drawable.bg_admin_chip);
                chip.setTextColor(getColor(R.color.text_dark_tertiary));
            }
            
            chip.setText(c.name);
            chip.setTextSize(12);
            chip.setTypeface(null, android.graphics.Typeface.BOLD);
            chip.setAllCaps(false);
            
            chip.setOnClickListener(v -> selectCategory(c.name, chip));
            
            filterRow.addView(chip);
            dynamicChips.add(chip);
        }
        
        // Reset trạng thái chọn của nút All nếu All đang active
        if (currentCategory.equals("All")) {
            filterAll.setBackgroundResource(R.drawable.bg_admin_chip_selected);
            filterAll.setTextColor(getColor(R.color.brand_white));
        } else {
            filterAll.setBackgroundResource(R.drawable.bg_admin_chip);
            filterAll.setTextColor(getColor(R.color.text_dark_tertiary));
        }
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
        for (TextView chip : dynamicChips) {
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
        startActivity(new Intent(this, AdminAddProductActivity.class));
    }

    @Override
    public void onEditClick(Product product, int position) {
        Intent intent = new Intent(this, AdminAddProductActivity.class);
        intent.putExtra("PRODUCT_ID", product.getId());
        startActivity(intent);
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