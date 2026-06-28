package com.example.shoeapp.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.R;
import com.example.shoeapp.admin.AdminDashboardActivity;
import com.example.shoeapp.data.entity.Brand;
import com.example.shoeapp.data.entity.Category;
import com.example.shoeapp.model.Product;
import com.example.shoeapp.ui.BaseSoleStepActivity;
import com.example.shoeapp.ui.BottomNavHelper;
import androidx.viewpager2.widget.ViewPager2;
import android.os.Handler;
import android.os.Looper;

import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.entity.Promotion;
import com.example.shoeapp.user.adapter.ClientProductAdapter;
import com.example.shoeapp.user.adapter.ProductGridSpacingDecoration;
import com.example.shoeapp.user.adapter.PromotionBannerAdapter;

import java.util.List;

public class MainActivity extends BaseSoleStepActivity {
    private ViewPager2 bannerViewPager;
    private LinearLayout sliderDots;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (bannerViewPager != null && bannerViewPager.getAdapter() != null) {
                int count = bannerViewPager.getAdapter().getItemCount();
                if (count > 0) {
                    int next = (bannerViewPager.getCurrentItem() + 1) % count;
                    bannerViewPager.setCurrentItem(next, true);
                }
                sliderHandler.postDelayed(this, 3000);
            }
        }
    };
    private ClientProductRepository productRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        productRepository = new ClientProductRepository(this);
        productRepository.ensureSeedData();

        setupScreen(BottomNavHelper.TAG_HOME);
        setupSearch();
        setupPromotions();
        setupCategories();
        setupProductGrid();
        setupBrands();
        setupSeeAll();
        setupChatbotFAB();
    }

    private void setupPromotions() {
        bannerViewPager = findViewById(R.id.home_banner_viewpager);
        sliderDots = findViewById(R.id.home_slider_dots);
        
        List<Promotion> activePromotions = AppDatabase.getDatabase(this).productDao().getBannerPromotions();
        
        if (activePromotions != null && !activePromotions.isEmpty() && bannerViewPager != null) {
            PromotionBannerAdapter adapter = new PromotionBannerAdapter(activePromotions, promotion -> {
                showPromotionBottomSheet(promotion);
            });
            bannerViewPager.setAdapter(adapter);
            setupDots(activePromotions.size());
            
            bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    updateDots(position);
                    sliderHandler.removeCallbacks(sliderRunnable);
                    sliderHandler.postDelayed(sliderRunnable, 3000);
                }
            });
        }
    }

    private void showPromotionBottomSheet(Promotion promotion) {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_promotion, null);
        bottomSheetDialog.setContentView(view);

        ImageView ivCover = view.findViewById(R.id.iv_promo_cover);
        TextView tvTitle = view.findViewById(R.id.tv_promo_title);
        TextView tvDescription = view.findViewById(R.id.tv_promo_description);
        TextView tvVoucherCode = view.findViewById(R.id.tv_promo_voucher_code);
        android.widget.Button btnBuyNow = view.findViewById(R.id.btn_buy_now);

        tvTitle.setText(promotion.name);
        tvDescription.setText(promotion.description != null ? promotion.description : promotion.subtitle);
        tvVoucherCode.setText(promotion.voucherCode != null ? promotion.voucherCode : "");

        if (promotion.bannerUrl != null && !promotion.bannerUrl.isEmpty()) {
            com.bumptech.glide.Glide.with(this).load(promotion.bannerUrl).into(ivCover);
        } else {
            ivCover.setImageResource(R.drawable.ic_shoe);
        }

        btnBuyNow.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Intent intent = new Intent(this, CatalogActivity.class);
            intent.putExtra(CatalogActivity.EXTRA_TITLE, promotion.name);
            intent.putExtra("promotion_id", promotion.id);
            startActivity(intent);
        });

        bottomSheetDialog.show();
    }

    private void setupDots(int count) {
        if (sliderDots == null) return;
        sliderDots.removeAllViews();
        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(6), dp(6));
            if (i > 0) params.setMarginStart(dp(4));
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.bg_rounded_gray);
            sliderDots.addView(dot);
        }
        if (count > 0) updateDots(0);
    }

    private void updateDots(int position) {
        if (sliderDots == null) return;
        for (int i = 0; i < sliderDots.getChildCount(); i++) {
            View dot = sliderDots.getChildAt(i);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dot.getLayoutParams();
            if (i == position) {
                params.width = dp(16);
                dot.setBackgroundResource(R.drawable.bg_button_primary);
            } else {
                params.width = dp(6);
                dot.setBackgroundResource(R.drawable.bg_rounded_gray);
            }
            dot.setLayoutParams(params);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    private void setupSearch() {
        android.widget.EditText searchInput = findViewById(R.id.home_search_input);
        findViewById(R.id.home_search_button).setOnClickListener(v -> {
            String keyword = searchInput.getText().toString();
            Intent intent = new Intent(this, CatalogActivity.class);
            intent.putExtra(CatalogActivity.EXTRA_TITLE, "Tìm kiếm sản phẩm");
            intent.putExtra(CatalogActivity.EXTRA_KEYWORD, keyword);
            startActivity(intent);
        });

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                findViewById(R.id.home_search_button).performClick();
                return true;
            }
            return false;
        });
    }

    private void setupCategories() {
        LinearLayout container = findViewById(R.id.home_categories_container);
        if (container == null) return;
        container.removeAllViews();

        List<Category> categories = productRepository.getCategories();

        for (int i = 0; i < categories.size(); i++) {
            Category cat = categories.get(i);
            LinearLayout item = buildCategoryItem(cat.name, i == 0);
            item.setOnClickListener(v -> openCatalogByCategory(cat.id, cat.name));
            if (i > 0) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) item.getLayoutParams();
                params.setMarginStart(dp(8));
                item.setLayoutParams(params);
            }
            container.addView(item);
        }
    }

    private LinearLayout buildCategoryItem(String name, boolean active) {
        LinearLayout item = new LinearLayout(this);
        item.setLayoutParams(new LinearLayout.LayoutParams(dp(78), dp(78)));
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setBackgroundResource(active
                ? R.drawable.bg_home_category_active
                : R.drawable.bg_home_category);

        ImageView icon = new ImageView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(28), dp(28));
        icon.setLayoutParams(iconParams);
        icon.setImageResource(R.drawable.ic_shoe);
        icon.setColorFilter(ContextCompat.getColor(this,
                active ? R.color.brand_white : R.color.status_info_medium));

        TextView label = new TextView(this);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        labelParams.topMargin = dp(4);
        label.setLayoutParams(labelParams);
        label.setText(name);
        label.setTextSize(10f);
        label.setTextColor(ContextCompat.getColor(this,
                active ? R.color.brand_white : R.color.text_secondary));
        label.setGravity(Gravity.CENTER);
        label.setMaxLines(2);
        label.setMaxWidth(dp(70));

        item.addView(icon);
        item.addView(label);
        return item;
    }

    private void setupProductGrid() {
        RecyclerView productGrid = findViewById(R.id.home_product_grid);
        productGrid.setLayoutManager(new GridLayoutManager(this, 2));
        List<Product> featured = productRepository.getFeaturedProducts();
        productGrid.setAdapter(new ClientProductAdapter(this, featured, this::openProductDetail));
        productGrid.addItemDecoration(new ProductGridSpacingDecoration(
                getResources().getDimensionPixelSize(R.dimen.space_6),
                getResources().getDimensionPixelSize(R.dimen.space_8)));
    }

    private void setupBrands() {
        LinearLayout container = findViewById(R.id.home_brands_container);
        if (container == null) return;
        container.removeAllViews();

        List<Brand> brands = productRepository.getBrands();
        for (int i = 0; i < brands.size(); i++) {
            Brand brand = brands.get(i);
            TextView chip = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, dp(38));
            if (i > 0) params.setMarginStart(dp(6));
            chip.setLayoutParams(params);
            chip.setBackgroundResource(R.drawable.bg_home_brand_chip);
            chip.setGravity(Gravity.CENTER);
            chip.setMinWidth(dp(86));
            chip.setPadding(dp(10), 0, dp(10), 0);
            chip.setText(brand.name);
            chip.setTextSize(12f);
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));

            chip.setOnClickListener(v -> {
                Intent intent = new Intent(this, CatalogActivity.class);
                intent.putExtra(CatalogActivity.EXTRA_TITLE, brand.name);
                intent.putExtra(CatalogActivity.EXTRA_KEYWORD, brand.name);
                startActivity(intent);
            });

            container.addView(chip);
        }
    }

    private void setupSeeAll() {
        // nút "xem tất cả" ở phần danh mục
        View categoriesSeeAll = findViewById(R.id.home_categories_see_all);
        if (categoriesSeeAll != null) {
            categoriesSeeAll.setOnClickListener(v -> openCatalogAll());
        }
        // nút "xem tất cả" ở phần sản phẩm nổi bật
        View trendingSeeAll = findViewById(R.id.home_trending_see_all);
        if (trendingSeeAll != null) {
            trendingSeeAll.setOnClickListener(v -> openCatalogAll());
        }
    }

    private void openCatalogAll() {
        Intent intent = new Intent(this, CatalogActivity.class);
        intent.putExtra(CatalogActivity.EXTRA_TITLE, "Tất cả sản phẩm");
        startActivity(intent);
    }

    private void openCatalogByCategory(int categoryId, String categoryName) {
        Intent intent = new Intent(this, CatalogActivity.class);
        intent.putExtra(CatalogActivity.EXTRA_CATEGORY_ID, categoryId);
        intent.putExtra(CatalogActivity.EXTRA_TITLE, categoryName);
        startActivity(intent);
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }

    private void setupChatbotFAB() {
        View fabChat = findViewById(R.id.fab_chat);
        if (fabChat != null) {
            fabChat.setOnClickListener(v -> {
                Intent intent = new Intent(this, ChatActivity.class);
                startActivity(intent);
            });
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
