package com.example.shoeapp.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.R;
import com.example.shoeapp.model.Product;
import com.example.shoeapp.ui.BaseSoleStepActivity;
import com.example.shoeapp.ui.BottomNavHelper;
import com.example.shoeapp.user.adapter.ClientProductAdapter;
import com.example.shoeapp.user.adapter.ProductGridSpacingDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CatalogActivity extends BaseSoleStepActivity {
    public static final String EXTRA_KEYWORD = "search_keyword";
    public static final String EXTRA_CATEGORY_ID = "category_id";
    public static final String EXTRA_TITLE = "catalog_title";

    private ClientProductRepository productRepository;
    private ClientProductAdapter adapter;
    private List<Product> allProducts = new ArrayList<>();
    private String currentSort = "default";
    private boolean isPriceAsc = true;

    private TextView sortDefault, sortPrice, sortCategory, sortNewest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        productRepository = new ClientProductRepository(this);
        productRepository.ensureSeedData();

        setupScreen(BottomNavHelper.TAG_SEARCH);
        setupTitle();
        setupProductGrid();
        setupSearch();
        setupSortChips();
        loadProducts();
    }

    private void setupTitle() {
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        if (title != null) {
            ((TextView) findViewById(R.id.catalog_title_text)).setText(title);
        }
    }

    private void setupProductGrid() {
        RecyclerView productGrid = findViewById(R.id.catalog_product_grid);
        productGrid.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ClientProductAdapter(this, new ArrayList<>(), this::openProductDetail);
        productGrid.setAdapter(adapter);
        productGrid.addItemDecoration(new ProductGridSpacingDecoration(
                getResources().getDimensionPixelSize(R.dimen.space_6),
                getResources().getDimensionPixelSize(R.dimen.space_8)));
    }

    private void setupSearch() {
        EditText searchInput = findViewById(R.id.catalog_search_input);

        // nếu được mở từ trang chủ kèm từ khóa thì điền sẵn vào ô tìm kiếm
        String keyword = getIntent().getStringExtra(EXTRA_KEYWORD);
        if (keyword != null && !keyword.isEmpty()) {
            searchInput.setText(keyword);
            searchInput.setSelection(keyword.length());
        }

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                applyFilters(s.toString());
            }
        });
    }

    private void setupSortChips() {
        sortDefault = findViewById(R.id.sort_default);
        sortPrice = findViewById(R.id.sort_price);
        sortCategory = findViewById(R.id.sort_category);
        sortNewest = findViewById(R.id.sort_newest);

        sortDefault.setOnClickListener(v -> selectSort("default"));
        sortPrice.setOnClickListener(v -> {
            isPriceAsc = !isPriceAsc;
            sortPrice.setText(isPriceAsc ? "Giá ↑" : "Giá ↓");
            selectSort(isPriceAsc ? "price_asc" : "price_desc");
        });
        sortCategory.setOnClickListener(v -> selectSort("category"));
        sortNewest.setOnClickListener(v -> selectSort("newest"));
    }

    private void loadProducts() {
        Intent intent = getIntent();
        int categoryId = intent.getIntExtra(EXTRA_CATEGORY_ID, -1);

        if (categoryId > 0) {
            allProducts = productRepository.getProductsByCategory(categoryId);
        } else {
            String keyword = intent.getStringExtra(EXTRA_KEYWORD);
            if (keyword != null && !keyword.isEmpty()) {
                allProducts = productRepository.searchProducts(keyword);
            } else {
                allProducts = productRepository.getAllProducts();
            }
        }

        applyFilters(((EditText) findViewById(R.id.catalog_search_input)).getText().toString());
    }

    private void applyFilters(String keyword) {
        List<Product> filtered = new ArrayList<>();
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();

        for (Product p : allProducts) {
            if (kw.isEmpty()
                    || p.getName().toLowerCase().contains(kw)
                    || p.getBrand().toLowerCase().contains(kw)) {
                filtered.add(p);
            }
        }

        // sắp xếp theo lựa chọn hiện tại
        switch (currentSort) {
            case "price_asc":
                Collections.sort(filtered, (a, b) -> Double.compare(a.getPrice(), b.getPrice()));
                break;
            case "price_desc":
                Collections.sort(filtered, (a, b) -> Double.compare(b.getPrice(), a.getPrice()));
                break;
            case "category":
                Collections.sort(filtered, (a, b) -> a.getCategory().compareTo(b.getCategory()));
                break;
            case "newest":
                Collections.sort(filtered, (a, b) -> b.getId() - a.getId());
                break;
        }

        adapter.updateProducts(filtered);
        ((TextView) findViewById(R.id.catalog_count_text)).setText(filtered.size() + " sản phẩm");
    }

    private void selectSort(String sort) {
        currentSort = sort;
        int activeRes = R.drawable.bg_catalog_chip_selected;
        int inactiveRes = R.drawable.bg_home_brand_chip;
        int activeColor = getColor(R.color.brand_orange);
        int inactiveColor = getColor(R.color.text_secondary);

        boolean isPrice = sort.startsWith("price");
        sortDefault.setBackgroundResource("default".equals(sort) ? activeRes : inactiveRes);
        sortPrice.setBackgroundResource(isPrice ? activeRes : inactiveRes);
        sortCategory.setBackgroundResource("category".equals(sort) ? activeRes : inactiveRes);
        sortNewest.setBackgroundResource("newest".equals(sort) ? activeRes : inactiveRes);

        sortDefault.setTextColor("default".equals(sort) ? activeColor : inactiveColor);
        sortPrice.setTextColor(isPrice ? activeColor : inactiveColor);
        sortCategory.setTextColor("category".equals(sort) ? activeColor : inactiveColor);
        sortNewest.setTextColor("newest".equals(sort) ? activeColor : inactiveColor);

        applyFilters(((EditText) findViewById(R.id.catalog_search_input)).getText().toString());
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }
}
