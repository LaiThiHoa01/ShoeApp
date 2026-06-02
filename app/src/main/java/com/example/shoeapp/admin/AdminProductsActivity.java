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
import java.util.Arrays;
import java.util.List;


public class AdminProductsActivity extends AppCompatActivity
        implements AdminProductAdapter.OnProductActionListener {

    // ── Views ────────────────────────────────────────────────────────────────
    private RecyclerView        recyclerView;
    private EditText            searchInput;
    private TextView            filterAll, filterSneakers, filterRunning, filterCasual;
    private BottomNavigationView bottomNav;

    // ── Data ─────────────────────────────────────────────────────────────────
    private AdminProductAdapter adapter;
    private List<Product>       allProducts;
    private List<Product>       filteredProducts;

    private String currentCategory = "All";
    private String currentSearch   = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_products);
        setupEdgeToEdge();
        bindViews();
        setupData();
        setupRecyclerView();
        setupSearch();
        setupFilterChips();
        setupBottomNav();
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
        bottomNav = findViewById(R.id.admin_bottom_nav);

        // Nút Add Product
        findViewById(R.id.admin_products_btn_add)
                .setOnClickListener(v -> onAddProductClick());
    }

    private void setupData() {
        allProducts = new ArrayList<>();
        allProducts.add(new Product(
                1, "Air Phantom Pro", "NovaSole · Sneakers", "Sneakers",
                189.99, 249.99, 45, true,
                Arrays.asList(7, 8, 9, 10, 11), 4.8f, 234,
                R.drawable.ic_shoe));

        allProducts.add(new Product(
                2, "Urban Stride X", "StreetFlex · Sneakers", "Sneakers",
                149.99, 199.99, 30, false,
                Arrays.asList(7, 8, 9, 10), 4.6f, 189,
                R.drawable.ic_shoe));

        allProducts.add(new Product(
                3, "Blaze Runner", "SwiftKick · Running", "Running",
                219.99, 279.99, 60, true,
                Arrays.asList(6, 7, 8, 9, 10), 4.9f, 412,
                R.drawable.ic_shoe));

        allProducts.add(new Product(
                4, "Cloud Walker", "FeatherStep · Casual", "Casual",
                99.99, 129.99, 12, false,
                Arrays.asList(7, 8, 9, 10), 4.3f, 97,
                R.drawable.ic_shoe));

        allProducts.add(new Product(
                5, "Shadow Force", "DarkLine · Sneakers", "Sneakers",
                259.99, 319.99, 8, false,
                Arrays.asList(8, 9, 10, 11), 4.7f, 311,
                R.drawable.ic_shoe));

        allProducts.add(new Product(
                6, "Apex Boost", "ProStride · Running", "Running",
                179.99, 229.99, 25, true,
                Arrays.asList(7, 8, 9, 10, 11, 12), 4.5f, 156,
                R.drawable.ic_shoe));

        filteredProducts = new ArrayList<>(allProducts);
    }

    private void setupRecyclerView() {
        adapter = new AdminProductAdapter(this, filteredProducts, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Khoảng cách giữa các item
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
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_categories) {
                startActivity(new Intent(this, AdminCategoriesActivity.class));
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
            return id == R.id.nav_products;
        });
    }

    private void selectCategory(String category, TextView selectedChip) {
        currentCategory = category;

        // Reset tất cả chip về inactive
        TextView[] chips = { filterAll, filterSneakers, filterRunning, filterCasual };
        for (TextView chip : chips) {
            chip.setBackgroundResource(R.drawable.bg_admin_chip);
            chip.setTextColor(getColor(R.color.text_dark_tertiary));
        }
        // Active chip được chọn
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

            if (matchCategory && matchSearch) {
                filteredProducts.add(p);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void onAddProductClick() {
        // TODO: startActivity(new Intent(this, AdminAddProductActivity.class));
        Toast.makeText(this, "Tính năng thêm sản phẩm đang được phát triển", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onEditClick(Product product, int position) {
        // TODO: startActivity(new Intent(this, AdminEditProductActivity.class)
        //           .putExtra("product_id", product.getId()));
        Toast.makeText(this,
                "Sửa: " + product.getName(),
                Toast.LENGTH_SHORT).show();
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
        // Xóa khỏi danh sách gốc
        allProducts.remove(product);
        // Áp lại filter để cập nhật list hiển thị
        applyFilters();
        Toast.makeText(this,
                "Đã xóa \"" + product.getName() + "\"",
                Toast.LENGTH_SHORT).show();
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