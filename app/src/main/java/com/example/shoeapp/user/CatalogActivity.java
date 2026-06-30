package com.example.shoeapp.user;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.R;
import com.example.shoeapp.data.entity.Category;
import com.example.shoeapp.model.Product;
import com.example.shoeapp.ui.BaseSoleStepActivity;
import com.example.shoeapp.ui.BottomNavHelper;
import com.example.shoeapp.user.adapter.ClientProductAdapter;
import com.example.shoeapp.user.adapter.ProductGridSpacingDecoration;
import com.example.shoeapp.user.adapter.SearchSuggestionAdapter;

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
    private int selectedCategoryId = -1;   // -1 = không lọc danh mục
    private String selectedCategoryName = "";
    private List<Category> categories = new ArrayList<>();

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
        androidx.cardview.widget.CardView cardSuggestions = findViewById(R.id.card_search_suggestions);
        RecyclerView rvSuggestions = findViewById(R.id.rv_search_suggestions);

        // nếu được mở từ trang chủ kèm từ khóa thì điền sẵn vào ô tìm kiếm
        String keyword = getIntent().getStringExtra(EXTRA_KEYWORD);
        if (keyword != null && !keyword.isEmpty()) {
            searchInput.setText(keyword);
            searchInput.setSelection(keyword.length());
        }

        if (rvSuggestions != null && cardSuggestions != null) {
            // thiết lập hiển thị danh sách cho phần gợi ý tìm kiếm
            rvSuggestions.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            SearchSuggestionAdapter suggestionAdapter = new SearchSuggestionAdapter(product -> {
                // điền nhanh từ khóa gợi ý vào ô nhập và tiến hành lọc trực tiếp
                searchInput.setText(product.getName());
                searchInput.setSelection(product.getName().length());
                cardSuggestions.setVisibility(android.view.View.GONE);
                applyFilters(product.getName());
            });
            rvSuggestions.setAdapter(suggestionAdapter);

            searchInput.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    String query = s.toString().trim();
                    // lọc lưới sản phẩm bên dưới theo thời gian thực
                    applyFilters(s.toString());

                    if (query.isEmpty()) {
                        cardSuggestions.setVisibility(android.view.View.GONE);
                    } else {
                        // chạy luồng phụ tìm kiếm sản phẩm phù hợp làm gợi ý
                        new Thread(() -> {
                            List<Product> suggestions = productRepository.searchProducts(query);
                            if (suggestions.size() > 5) {
                                suggestions = suggestions.subList(0, 5); // giới hạn tối đa 5 gợi ý
                            }
                            final List<Product> finalSuggestions = suggestions;
                            runOnUiThread(() -> {
                                if (finalSuggestions.isEmpty() || searchInput.getText().toString().trim().isEmpty()) {
                                    cardSuggestions.setVisibility(android.view.View.GONE);
                                } else {
                                    suggestionAdapter.updateList(finalSuggestions);
                                    cardSuggestions.setVisibility(android.view.View.VISIBLE);
                                }
                            });
                        }).start();
                    }
                }
            });

            // ẩn phần gợi ý và đóng bàn phím khi người dùng ấn nút tìm kiếm
            searchInput.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                    cardSuggestions.setVisibility(android.view.View.GONE);
                    android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
                    }
                    return true;
                }
                return false;
            });
        } else {
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    applyFilters(s.toString());
                }
            });
        }
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
        sortCategory.setOnClickListener(v -> showCategoryDialog());
        sortNewest.setOnClickListener(v -> selectSort("newest"));
    }

    private void showCategoryDialog() {
        if (categories.isEmpty()) {
            categories = productRepository.getCategories();
        }

        // Tạo danh sách tên danh mục, thêm "Tất cả" ở đầu
        String[] names = new String[categories.size() + 1];
        names[0] = "Tất cả danh mục";
        for (int i = 0; i < categories.size(); i++) {
            names[i + 1] = categories.get(i).name;
        }

        // Đánh dấu item đang chọn
        int checkedItem = 0;
        if (selectedCategoryId != -1) {
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).id == selectedCategoryId) {
                    checkedItem = i + 1;
                    break;
                }
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Chọn danh mục")
                .setSingleChoiceItems(names, checkedItem, (dialog, which) -> {
                    dialog.dismiss();
                    if (which == 0) {
                        // Bỏ lọc danh mục
                        selectedCategoryId = -1;
                        selectedCategoryName = "";
                        sortCategory.setText("Danh mục");
                        selectSort("default");
                    } else {
                        Category cat = categories.get(which - 1);
                        selectedCategoryId = cat.id;
                        selectedCategoryName = cat.name;
                        sortCategory.setText(cat.name + " ▾");
                        selectSort("category");
                    }
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void loadProducts() {
        Intent intent = getIntent();
        int categoryId = intent.getIntExtra(EXTRA_CATEGORY_ID, -1);
        int promotionId = intent.getIntExtra("promotion_id", -1);

        if (promotionId > 0) {
            allProducts = productRepository.getProductsByPromotion(promotionId);
        } else if (categoryId > 0) {
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
            // Lọc theo từ khóa
            boolean matchKeyword = kw.isEmpty()
                    || p.getName().toLowerCase().contains(kw)
                    || p.getBrand().toLowerCase().contains(kw);

            // Lọc theo danh mục đã chọn
            boolean matchCategory = selectedCategoryId == -1
                    || (p.getCategory() != null && p.getCategory().equals(selectedCategoryName));

            if (matchKeyword && matchCategory) {
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
            case "newest":
                Collections.sort(filtered, (a, b) -> b.getId() - a.getId());
                break;
            default:
                Collections.sort(filtered, (a, b) -> {
                    int compareReviews = Integer.compare(b.getReviewCount(), a.getReviewCount());
                    if (compareReviews == 0) {
                        return Float.compare(b.getRating(), a.getRating());
                    }
                    return compareReviews;
                });
                break;
        }

        adapter.updateProducts(filtered);
        ((TextView) findViewById(R.id.catalog_count_text)).setText(filtered.size() + " sản phẩm");

        // hiển thị thông báo "không tìm thấy sản phẩm" khi kết quả lọc trống
        View emptyState = findViewById(R.id.layout_empty_state);
        View productGrid = findViewById(R.id.catalog_product_grid);
        if (emptyState != null && productGrid != null) {
            if (filtered.isEmpty()) {
                emptyState.setVisibility(android.view.View.VISIBLE);
                productGrid.setVisibility(android.view.View.GONE);
            } else {
                emptyState.setVisibility(android.view.View.GONE);
                productGrid.setVisibility(android.view.View.VISIBLE);
            }
        }
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
