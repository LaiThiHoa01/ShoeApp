package com.example.shoeapp.user;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shoeapp.R;
import com.example.shoeapp.data.entity.Product;
import com.example.shoeapp.data.entity.ProductVariant;
import com.example.shoeapp.data.model.ProductColorOption;
import com.example.shoeapp.data.model.ProductSizeOption;
import com.example.shoeapp.data.repo.CartRepository;
import com.example.shoeapp.data.repo.ProductRepository;
import com.example.shoeapp.ui.BaseSoleStepActivity;
import com.example.shoeapp.ui.BottomNavHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends BaseSoleStepActivity {
    private ProductRepository productRepository;
    private CartRepository cartRepository;
    private Product product;
    private com.example.shoeapp.model.Product clientProduct;
    private final List<TextView> sizeViews = new ArrayList<>();
    private final List<View> colorViews = new ArrayList<>();
    private int selectedColorId;
    private int selectedSizeId;
    private int selectedStock;
    private int quantity = 1;
    private final List<View> thumbnailViews = new ArrayList<>();
    private final List<ProductColorOption> colorOptionsForThumbnails = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        productRepository = new ProductRepository(this);
        cartRepository = new CartRepository(this);
        productRepository.ensureSeedData();

        setupScreen(BottomNavHelper.TAG_SEARCH);
        bindProduct();
    }

    private void bindProduct() {
        int productId = getIntent().getIntExtra("product_id", -1);
        if (productId == -1) {
            finish();
            return;
        }
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

        ((TextView) findViewById(R.id.detail_rating_text))
                .setText(String.format(Locale.US, "%.1f", clientProduct.getRating()));
        ((TextView) findViewById(R.id.detail_review_count_text))
                .setText(String.format(Locale.US, "(%d đánh giá)", clientProduct.getReviewCount()));

        int ratingInt = Math.round(clientProduct.getRating());
        StringBuilder starsStr = new StringBuilder();
        for (int i = 0; i < ratingInt; i++) starsStr.append("★");
        for (int i = ratingInt; i < 5; i++) starsStr.append("☆");
        ((TextView) findViewById(R.id.detail_rating_stars)).setText(starsStr.toString());
        ((TextView) findViewById(R.id.detail_description_text)).setText(product.description);
        findViewById(R.id.detail_badge_new).setVisibility(clientProduct.isNew() ? View.VISIBLE : View.GONE);

        setupColorOptions();
        setupThumbnails();
        setupQuantityControls();
        loadReviews();
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
        updateThumbnailSelection();
        setupSizeOptions();
    }

    private void setupSizeOptions() {
        GridLayout sizeContainer = findViewById(R.id.detail_size_options);
        sizeContainer.removeAllViews();
        sizeViews.clear();

        List<ProductSizeOption> sizes = productRepository.getAvailableSizes(product.id, selectedColorId);
        selectedSizeId = 0;

        for (ProductSizeOption size : sizes) {
            TextView sizeView = (TextView) getLayoutInflater()
                    .inflate(R.layout.item_detail_size_option, sizeContainer, false);

            sizeView.setText(size.name);
            sizeView.setEnabled(size.stock > 0);
            sizeView.setSelected(false);

            sizeView.setOnClickListener(v -> {
                if (v.isEnabled()) {
                    selectSize(size);
                }
            });

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
        if (selectedStock <= 0) {
            quantity = 1;
        } else {
            quantity = Math.min(quantity, selectedStock);
        }

        List<ProductSizeOption> sizes = productRepository.getAvailableSizes(product.id, selectedColorId);

        for (int i = 0; i < sizes.size() && i < sizeViews.size(); i++) {
            ProductSizeOption option = sizes.get(i);
            TextView sizeView = sizeViews.get(i);

            boolean selected = option.id == selectedSizeId;

            sizeView.setSelected(selected);
            sizeView.setEnabled(option.stock > 0);
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
        ((TextView) findViewById(R.id.add_to_cart_button))
                .setText("Thêm vào giỏ - " + productRepository.formatPrice(product.price * quantity));
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

    private void setupThumbnails() {
        LinearLayout thumbnailContainer = findViewById(R.id.detail_thumbnail_container);

        if (thumbnailContainer == null) {
            return;
        }

        thumbnailContainer.removeAllViews();
        thumbnailViews.clear();
        colorOptionsForThumbnails.clear();

        List<ProductColorOption> colors = productRepository.getAvailableColors(product.id);
        colorOptionsForThumbnails.addAll(colors);

        for (int i = 0; i < colors.size(); i++) {
            ProductColorOption color = colors.get(i);

            FrameLayout frameLayout = (FrameLayout) getLayoutInflater()
                    .inflate(R.layout.item_detail_thumbnail, thumbnailContainer, false);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) frameLayout.getLayoutParams();

            if (i > 0) {
                params.setMarginStart(dp(6));
            }

            frameLayout.setLayoutParams(params);

            ImageView thumbImage = frameLayout.findViewById(R.id.detail_thumbnail_image);

            String imgUrl = productRepository.getImageUrl(product.id, color.id);
            ImageLoader.load(imgUrl, thumbImage, clientProduct.getImageResId());

            frameLayout.setSelected(false);
            frameLayout.setOnClickListener(v -> selectColor(color));

            thumbnailContainer.addView(frameLayout);
            thumbnailViews.add(frameLayout);
        }

        updateThumbnailSelection();
    }

    private void updateThumbnailSelection() {
        for (int i = 0; i < colorOptionsForThumbnails.size() && i < thumbnailViews.size(); i++) {
            boolean selected = colorOptionsForThumbnails.get(i).id == selectedColorId;
            thumbnailViews.get(i).setSelected(selected);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void loadReviews() {
        LinearLayout reviewsContainer = findViewById(R.id.detail_reviews_container);
        if (reviewsContainer == null)
            return;
        reviewsContainer.removeAllViews();

        List<com.example.shoeapp.data.entity.ProductReview> reviews = productRepository.getReviewsByProduct(product.id);

        if (reviews.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Chưa có đánh giá nào.");
            emptyText.setTextColor(getColor(R.color.text_secondary));
            emptyText.setPadding(0, dp(16), 0, dp(16));
            reviewsContainer.addView(emptyText);
            return;
        }

        for (com.example.shoeapp.data.entity.ProductReview review : reviews) {
            View reviewView = getLayoutInflater().inflate(R.layout.item_product_review, reviewsContainer, false);

            TextView avatar = reviewView.findViewById(R.id.review_avatar_text);
            TextView name = reviewView.findViewById(R.id.review_author_name);
            TextView stars = reviewView.findViewById(R.id.review_stars);
            TextView date = reviewView.findViewById(R.id.review_date);
            TextView content = reviewView.findViewById(R.id.review_content);

            com.example.shoeapp.data.entity.User user = productRepository.getUserById(this, review.userId);
            if (user != null && user.fullName != null && !user.fullName.isEmpty()) {
                name.setText(user.fullName);
                avatar.setText(user.fullName.substring(0, 1).toUpperCase());
            } else {
                name.setText("Khách");
                avatar.setText("K");
            }

            StringBuilder starsStr = new StringBuilder();
            for (int i = 0; i < review.rating; i++)
                starsStr.append("★");
            for (int i = review.rating; i < 5; i++)
                starsStr.append("☆");
            stars.setText(starsStr.toString());

            if (review.createdAt != null) {
                try {
                    java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault());
                    java.util.Date d = inputFormat.parse(review.createdAt);
                    java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("dd MMM",
                            new Locale("vi", "VN"));
                    date.setText(outputFormat.format(d));
                } catch (Exception e) {
                    date.setText(review.createdAt);
                }
            } else {
                date.setText("");
            }

            content.setText(review.content);

            ImageView reviewImage = reviewView.findViewById(R.id.review_image);
            if (reviewImage != null) {
                if (review.imageUrl != null && !review.imageUrl.trim().isEmpty()) {
                    reviewImage.setVisibility(View.VISIBLE);
                    ImageLoader.load(review.imageUrl, reviewImage, R.drawable.ic_shoe);
                } else {
                    reviewImage.setVisibility(View.GONE);
                }
            }

            reviewsContainer.addView(reviewView);
        }
    }
}
