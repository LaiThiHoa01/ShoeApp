package com.example.shoeapp.user;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shoeapp.R;
import com.example.shoeapp.data.entity.Product;
import com.example.shoeapp.ui.BaseSoleStepActivity;
import com.example.shoeapp.ui.BottomNavHelper;

import java.util.Locale;

public class ProductDetailActivity extends BaseSoleStepActivity {
    private ClientProductRepository productRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        productRepository = new ClientProductRepository(this);
        productRepository.ensureSeedData();

        setupScreen(BottomNavHelper.TAG_SEARCH);
        bindProduct();
    }

    private void bindProduct() {
        int productId = getIntent().getIntExtra("product_id", 1);
        Product product = productRepository.getProductById(productId);
        if (product == null) {
            finish();
            return;
        }

        com.example.shoeapp.model.Product clientProduct = productRepository.toClientProduct(product);

        ((TextView) findViewById(R.id.detail_brand_text)).setText(productRepository.getBrandName(product.brandId));
        ((TextView) findViewById(R.id.detail_name_text)).setText(product.name);
        ((TextView) findViewById(R.id.detail_price_text)).setText(productRepository.formatPrice(product.price));

        TextView originalPrice = findViewById(R.id.detail_original_price_text);
        originalPrice.setText(productRepository.formatPrice(product.originalPrice));
        originalPrice.setPaintFlags(originalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        ((TextView) findViewById(R.id.detail_rating_text)).setText(String.format(Locale.US, "%.1f", clientProduct.getRating()));
        ((TextView) findViewById(R.id.detail_review_count_text)).setText(String.format(Locale.US, "(%d reviews)", clientProduct.getReviewCount()));
        ((TextView) findViewById(R.id.detail_description_text)).setText(product.description);
        ((TextView) findViewById(R.id.add_to_cart_button)).setText("Thêm vào giỏ - " + productRepository.formatPrice(product.price));
        ImageLoader.load(clientProduct.getImageUrl(), (ImageView) findViewById(R.id.detail_product_image), clientProduct.getImageResId());
        findViewById(R.id.detail_badge_new).setVisibility(clientProduct.isNew() ? View.VISIBLE : View.GONE);
    }
}
