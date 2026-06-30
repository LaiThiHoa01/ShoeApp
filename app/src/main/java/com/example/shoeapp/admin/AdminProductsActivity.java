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

public class AdminProductsActivity extends BaseAdminActivity
        implements AdminProductAdapter.OnProductActionListener {

    private RecyclerView         recyclerView;
    private EditText             searchInput;
    private TextView             filterAll;
    private BottomNavigationView bottomNav;

    private AdminProductAdapter  adapter;
    private List<Product>        allProducts = new ArrayList<>();
    private List<Product>        filteredProducts = new ArrayList<>();
    private AppDatabase          db;

    private List<com.example.shoeapp.data.entity.Product> dbProducts = new ArrayList<>();
    private List<TextView> dynamicChips = new ArrayList<>();

    private TextView             filterStockAll, filterStockIn, filterStockOut;
    private String currentStockFilter = "All"; // "All", "InStock", "OutOfStock"

    private String currentCategory = "All";
    private String currentSearch   = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.activity.EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_products);
        db = AppDatabase.getDatabase(this);
        setupEdgeToEdge();
        bindViews();
        loadFromDb();
        setupRecyclerView();
        setupSearch();
        setupFilterChips();
        setupStockFilterChips();
        setupBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_products);
        }
        loadFromDb();
        setupFilterChips();
    }

    private void setupEdgeToEdge() {
        View root = findViewById(R.id.admin_products_root);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return WindowInsetsCompat.CONSUMED;
            });
        }
    }

    private void bindViews() {
        recyclerView   = findViewById(R.id.admin_products_recycler);
        searchInput    = findViewById(R.id.admin_products_search_input);
        filterAll      = findViewById(R.id.admin_filter_all);
        filterStockAll = findViewById(R.id.admin_filter_stock_all);
        filterStockIn  = findViewById(R.id.admin_filter_stock_in);
        filterStockOut = findViewById(R.id.admin_filter_stock_out);
        bottomNav      = findViewById(R.id.admin_bottom_nav);
        findViewById(R.id.admin_products_btn_add)
                .setOnClickListener(v -> onAddProductClick());
    }

    private void loadFromDb() {
        new Thread(() -> {
            List<com.example.shoeapp.data.entity.Product> tempDbProducts = db.productDao().getAllProducts();
            List<Product> tempProducts = new ArrayList<>();

            if (tempDbProducts != null) {
                List<Brand> allBrands = db.productDao().getAllBrands();
                java.util.Map<Integer, String> brandMap = new java.util.HashMap<>();
                if (allBrands != null) {
                    for (Brand b : allBrands) {
                        brandMap.put(b.id, b.name);
                    }
                }

                List<Category> allCategories = db.categoryDao().getAllCategories();
                java.util.Map<Integer, String> categoryMap = new java.util.HashMap<>();
                if (allCategories != null) {
                    for (Category c : allCategories) {
                        categoryMap.put(c.id, c.name);
                    }
                }

                List<com.example.shoeapp.data.entity.Size> allSizes = db.productDao().getAllSizes();
                java.util.Map<Integer, com.example.shoeapp.data.entity.Size> sizeMap = new java.util.HashMap<>();
                if (allSizes != null) {
                    for (com.example.shoeapp.data.entity.Size s : allSizes) {
                        sizeMap.put(s.id, s);
                    }
                }

                List<com.example.shoeapp.data.entity.Color> allColors = db.productDao().getAllColors();
                java.util.Map<Integer, com.example.shoeapp.data.entity.Color> colorMap = new java.util.HashMap<>();
                if (allColors != null) {
                    for (com.example.shoeapp.data.entity.Color c : allColors) {
                        colorMap.put(c.id, c);
                    }
                }

                for (com.example.shoeapp.data.entity.Product entity : tempDbProducts) {
                    String brandName = brandMap.containsKey(entity.brandId) ? brandMap.get(entity.brandId) : "Thương hiệu không xác định";
                    String categoryName = categoryMap.containsKey(entity.shoeCategory) ? categoryMap.get(entity.shoeCategory) : "Không xác định";

                    List<com.example.shoeapp.data.entity.ProductVariant> variants =
                            db.productDao().getVariantsByProduct(entity.id);
                    int totalStock = 0;
                    List<Integer> sizeList = new ArrayList<>();
                    List<String> colorHexList = new ArrayList<>();
                    if (variants != null) {
                        for (com.example.shoeapp.data.entity.ProductVariant v : variants) {
                            totalStock += v.stock;
                            
                            com.example.shoeapp.data.entity.Size size = sizeMap.get(v.sizeId);
                            if (size != null) {
                                try {
                                    int szVal = Integer.parseInt(size.name.trim());
                                    if (!sizeList.contains(szVal)) {
                                        sizeList.add(szVal);
                                    }
                                } catch (NumberFormatException ignored) {}
                            }

                            com.example.shoeapp.data.entity.Color color = colorMap.get(v.colorId);
                            if (color != null && color.hexcode != null) {
                                String hex = color.hexcode.trim();
                                if (!colorHexList.contains(hex)) {
                                    colorHexList.add(hex);
                                }
                            }
                        }
                    }
                    Collections.sort(sizeList);

                    Float avgRating  = db.productDao().getAverageRating(entity.id);
                    float rating     = avgRating != null ? avgRating : 0.0f;
                    List<?> reviews  = db.productDao().getReviewsByProduct(entity.id);
                    int reviewCount  = reviews != null ? reviews.size() : 0;

                    String imageUrl = db.productDao().getThumbnailUrl(entity.id);

                    Product model = new Product(
                            entity.id,
                            entity.name,
                            brandName + " · " + categoryName,
                            categoryName,
                            entity.price,
                            entity.originalPrice > 0 ? entity.originalPrice : entity.price,
                            totalStock,
                            entity.isAvailable,
                            sizeList,
                            rating,
                            reviewCount,
                            R.drawable.ic_shoe,
                            imageUrl,
                            entity.isAvailable,
                            entity.isDiscontinue
                    );
                    model.setColors(colorHexList);
                    tempProducts.add(model);
                }
            }

            runOnUiThread(() -> {
                dbProducts = tempDbProducts;
                if (allProducts == null) {
                    allProducts = new ArrayList<>();
                }
                allProducts.clear();
                allProducts.addAll(tempProducts);

                if (filteredProducts == null) {
                    filteredProducts = new ArrayList<>(allProducts);
                } else {
                    filteredProducts.clear();
                    filteredProducts.addAll(allProducts);
                }
                if (adapter != null) applyFilters();
            });
        }).start();
    }

    private void setupRecyclerView() {
        adapter = new AdminProductAdapter(this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.submitList(new ArrayList<>(filteredProducts));
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

        if (dbCategories == null) return;
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
            Intent intent = null;
            if (id == R.id.nav_dashboard) {
                intent = new Intent(this, AdminDashboardActivity.class);
            } else if (id == R.id.nav_users) {
                intent = new Intent(this, UserManagementActivity.class);
            } else if (id == R.id.nav_categories) {
                intent = new Intent(this, AdminCategoriesActivity.class);
            } else if (id == R.id.nav_orders) {
                intent = new Intent(this, AdminOrderManagementActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }
            return id == R.id.nav_products;
        });

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(AdminProductsActivity.this, AdminDashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
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
                    || (p.getName() != null && p.getName().toLowerCase().contains(currentSearch))
                    || (p.getBrand() != null && p.getBrand().toLowerCase().contains(currentSearch));
            boolean matchStock = true;
            if (currentStockFilter.equals("InStock")) {
                matchStock = p.getStock() > 0;
            } else if (currentStockFilter.equals("OutOfStock")) {
                matchStock = p.getStock() == 0;
            }
            if (matchCategory && matchSearch && matchStock) filteredProducts.add(p);
        }
        adapter.submitList(new ArrayList<>(filteredProducts));
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
        if (product.isDiscontinued() || !product.isAvailable()) {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Khôi phục sản phẩm")
                    .setMessage("Bạn có muốn hiển thị lại sản phẩm \"" + product.getName() + "\" không?")
                    .setPositiveButton("Khôi phục", (dialog, which) -> restoreProduct(product))
                    .setNegativeButton("Hủy", null)
                    .show();
        } else {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Ẩn sản phẩm")
                    .setMessage("Bạn có chắc muốn ẩn \"" + product.getName() + "\" khỏi cửa hàng?")
                    .setPositiveButton("Ẩn", (dialog, which) -> deleteProduct(product))
                    .setNegativeButton("Hủy", null)
                    .show();
        }
    }

    @Override
    public void onVariantsClick(Product product, int position) {
        Intent intent = new Intent(this, AdminProductVariantActivity.class);
        intent.putExtra("PRODUCT_ID", product.getId());
        startActivity(intent);
    }

    private void deleteProduct(Product product) {
        new Thread(() -> {
            for (com.example.shoeapp.data.entity.Product entity : dbProducts) {
                if (entity.id == product.getId()) {
                    entity.isAvailable = false;
                    entity.isDiscontinue = true;
                    db.productDao().update(entity);
                    break;
                }
            }
            runOnUiThread(() -> {
                loadFromDb();
                Toast.makeText(this, "Đã ẩn \"" + product.getName() + "\"", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void restoreProduct(Product product) {
        new Thread(() -> {
            for (com.example.shoeapp.data.entity.Product entity : dbProducts) {
                if (entity.id == product.getId()) {
                    entity.isAvailable = true;
                    entity.isDiscontinue = false;
                    db.productDao().update(entity);
                    break;
                }
            }
            runOnUiThread(() -> {
                loadFromDb();
                Toast.makeText(this, "Đã khôi phục \"" + product.getName() + "\"", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void setupStockFilterChips() {
        filterStockAll.setOnClickListener(v -> selectStockFilter("All"));
        filterStockIn.setOnClickListener(v -> selectStockFilter("InStock"));
        filterStockOut.setOnClickListener(v -> selectStockFilter("OutOfStock"));
    }

    private void selectStockFilter(String filter) {
        currentStockFilter = filter;
        
        filterStockAll.setBackgroundResource("All".equals(filter)
                ? R.drawable.bg_admin_chip_selected : R.drawable.bg_admin_chip);
        filterStockAll.setTextColor("All".equals(filter)
                ? getColor(R.color.brand_white) : getColor(R.color.text_dark_tertiary));

        filterStockIn.setBackgroundResource("InStock".equals(filter)
                ? R.drawable.bg_admin_chip_selected : R.drawable.bg_admin_chip);
        filterStockIn.setTextColor("InStock".equals(filter)
                ? getColor(R.color.brand_white) : getColor(R.color.text_dark_tertiary));

        filterStockOut.setBackgroundResource("OutOfStock".equals(filter)
                ? R.drawable.bg_admin_chip_selected : R.drawable.bg_admin_chip);
        filterStockOut.setTextColor("OutOfStock".equals(filter)
                ? getColor(R.color.brand_white) : getColor(R.color.text_dark_tertiary));

        applyFilters();
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