package com.example.shoeapp.user;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shoeapp.R;
import com.example.shoeapp.data.entity.Product;
import com.example.shoeapp.data.entity.ProductVariant;
import com.example.shoeapp.data.model.ProductColorOption;
import com.example.shoeapp.data.model.ProductSizeOption;
import com.example.shoeapp.ui.BaseSoleStepActivity;
import com.example.shoeapp.ui.BottomNavHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends BaseSoleStepActivity {
    private ClientProductRepository productRepository;
    private ClientCartRepository cartRepository;
    private Product product;
    private com.example.shoeapp.model.Product clientProduct;
    private final List<TextView> sizeViews = new ArrayList<>();
    private final List<View> colorViews = new ArrayList<>();
    private int selectedColorId;
    private int selectedSizeId;
    private int selectedStock;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        productRepository = new ClientProductRepository(this);
        cartRepository = new ClientCartRepository(this);
        productRepository.ensureSeedData();

        setupScreen(BottomNavHelper.TAG_SEARCH);
        bindProduct();
    }

    private void bindProduct() {
        int productId = getIntent().getIntExtra("product_id", 1);
        product = productRepository.getProductById(productId);
        if (product == null) {
            finish();
            return;
        }

        clientProduct = productRepository.toClientProduct(product);
        ((TextView) findViewById(R.id.detail_brand_text)).setText(productRepository.getBrandName(product.brandId));
        ((TextView) findViewById(R.id.detail_name_text)).setText(product.name);
        ((TextView) findViewById(R.id.detail_price_text)).setText(productRepository.formatPrice(product.price));

        TextView originalPrice = findViewById(R.id.detail_original_price_text);
        originalPrice.setText(productRepository.formatPrice(product.originalPrice));
        originalPrice.setPaintFlags(originalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        ((TextView) findViewById(R.id.detail_rating_text)).setText(String.format(Locale.US, "%.1f", clientProduct.getRating()));
        ((TextView) findViewById(R.id.detail_review_count_text)).setText(String.format(Locale.US, "(%d đánh giá)", clientProduct.getReviewCount()));
        ((TextView) findViewById(R.id.detail_description_text)).setText(product.description);
        findViewById(R.id.detail_badge_new).setVisibility(clientProduct.isNew() ? View.VISIBLE : View.GONE);

        setupColorOptions();
        setupQuantityControls();
        findViewById(R.id.add_to_cart_button).setOnClickListener(v -> addToCart());
    }

    private void setupColorOptions() {
        LinearLayout colorContainer = findViewById(R.id.detail_color_options);
        colorContainer.removeAllViews();
        colorViews.clear();

        List<ProductColorOption> colors = productRepository.getAvailableColors(product.id);
        for (int i = 0; i < colors.size(); i++) {
            ProductColorOption color = colors.get(i);
            View swatch = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(34), dp(34));
            params.setMarginEnd(dp(8));
            swatch.setLayoutParams(params);
            swatch.setBackground(swatchBg(color.hexcode, i == 0));
            swatch.setOnClickListener(v -> selectColor(color));
            colorContainer.addView(swatch);
            colorViews.add(swatch);
        }

        if (!colors.isEmpty()) {
            selectColor(colors.get(0));
        }
    }

    private void selectColor(ProductColorOption color) {
        selectedColorId = color.id;
        List<ProductColorOption> colors = productRepository.getAvailableColors(product.id);
        for (int i = 0; i < colors.size() && i < colorViews.size(); i++) {
            colorViews.get(i).setBackground(swatchBg(colors.get(i).hexcode, colors.get(i).id == selectedColorId));
        }
        ImageLoader.load(productRepository.getImageUrl(product.id, selectedColorId),
                (ImageView) findViewById(R.id.detail_product_image),
                clientProduct.getImageResId());
        setupSizeOptions();
    }

    private void setupSizeOptions() {
        GridLayout sizeContainer = findViewById(R.id.detail_size_options);
        sizeContainer.removeAllViews();
        sizeViews.clear();

        List<ProductSizeOption> sizes = productRepository.getAvailableSizes(product.id, selectedColorId);
        selectedSizeId = 0;
        for (ProductSizeOption size : sizes) {
            TextView sizeView = new TextView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = dp(44);
            params.height = dp(44);
            params.setMargins(0, 0, dp(8), dp(8));
            sizeView.setLayoutParams(params);
            sizeView.setGravity(android.view.Gravity.CENTER);
            sizeView.setText(size.name);
            sizeView.setTextAppearance(this, R.style.TextAppearance_SoleStep_BodySmall);
            sizeView.setTextColor(getColor(size.stock > 0 ? R.color.text_secondary : R.color.text_disabled));
            sizeView.setBackgroundResource(R.drawable.bg_detail_size);
            sizeView.setEnabled(size.stock > 0);
            sizeView.setOnClickListener(v -> selectSize(size));
            sizeContainer.addView(sizeView);
            sizeViews.add(sizeView);
            if (selectedSizeId == 0 && size.stock > 0) {
                selectSize(size);
            }
        }
    }

    private void selectSize(ProductSizeOption size) {
        selectedSizeId = size.id;
        selectedStock = size.stock;
        quantity = Math.min(quantity, Math.max(1, selectedStock));

        List<ProductSizeOption> sizes = productRepository.getAvailableSizes(product.id, selectedColorId);
        for (int i = 0; i < sizes.size() && i < sizeViews.size(); i++) {
            ProductSizeOption option = sizes.get(i);
            TextView sizeView = sizeViews.get(i);
            boolean selected = option.id == selectedSizeId;
            sizeView.setBackgroundResource(selected ? R.drawable.bg_detail_size_selected : R.drawable.bg_detail_size);
            sizeView.setTextColor(getColor(selected ? R.color.brand_white : option.stock > 0 ? R.color.text_secondary : R.color.text_disabled));
            sizeView.setTypeface(null, selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }
        updateQuantityUi();
    }

    private void setupQuantityControls() {
        findViewById(R.id.detail_quantity_minus).setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantityUi();
            }
        });
        findViewById(R.id.detail_quantity_plus).setOnClickListener(v -> {
            if (quantity < selectedStock) {
                quantity++;
                updateQuantityUi();
            }
        });
    }

    private void updateQuantityUi() {
        ((TextView) findViewById(R.id.detail_quantity_text)).setText(String.valueOf(quantity));
        ((TextView) findViewById(R.id.detail_stock_text)).setText("Còn " + selectedStock + " sản phẩm");
        ((TextView) findViewById(R.id.add_to_cart_button)).setText("Thêm vào giỏ - " + productRepository.formatPrice(product.price * quantity));
    }

    private void addToCart() {
        if (selectedColorId == 0 || selectedSizeId == 0) {
            Toast.makeText(this, "Vui lòng chọn màu và size", Toast.LENGTH_SHORT).show();
            return;
        }
        ProductVariant variant = productRepository.getVariant(product.id, selectedColorId, selectedSizeId);
        if (variant == null || variant.stock <= 0 || quantity > variant.stock) {
            Toast.makeText(this, "Sản phẩm đã hết hàng ở lựa chọn này", Toast.LENGTH_SHORT).show();
            return;
        }
        cartRepository.addToCart(product.id, selectedColorId, selectedSizeId, quantity, product.price);
        Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
    }

    private GradientDrawable swatchBg(String hex, boolean selected) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(dp(12));
        try {
            drawable.setColor(Color.parseColor(hex));
        } catch (IllegalArgumentException ignored) {
            drawable.setColor(Color.LTGRAY);
        }
        drawable.setStroke(dp(selected ? 3 : 1), getColor(selected ? R.color.brand_orange : R.color.border_normal));
        return drawable;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
