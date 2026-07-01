package com.example.shoeapp.user;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.example.shoeapp.R;
import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.entity.Order;
import com.example.shoeapp.data.entity.ProductReview;
import com.example.shoeapp.data.model.OrderItemView;
import com.example.shoeapp.ui.BaseSoleStepActivity;
import com.example.shoeapp.ui.BottomNavHelper;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Date;
import java.util.Locale;

public class ProductReviewActivity extends BaseSoleStepActivity {
    private ImageButton[] stars;
    private TextView ratingLabel;
    private int rating = 4;
    private int productId = -1;
    private int userId = -1;
    private int orderId = -1;
    private androidx.activity.result.ActivityResultLauncher<String[]> pickImageLauncher;
    private String selectedImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_review);
        setupScreen(BottomNavHelper.TAG_ORDERS);

        productId = getIntent().getIntExtra("product_id", -1);
        userId = getIntent().getIntExtra("user_id", -1);
        orderId = getIntent().getIntExtra("order_id", -1);

        ExecutorService infoExecutor =
                Executors.newSingleThreadExecutor();

        infoExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            Order order = db.orderDao().getOrderById(orderId);
            List<OrderItemView> items = db.orderDao().getOrderItems(orderId);
            OrderItemView targetItem = null;
            if (items != null) {
                for (OrderItemView item : items) {
                    if (item.productId == productId) {
                        targetItem = item;
                        break;
                    }
                }
            }

            final OrderItemView finalItem = targetItem;

            runOnUiThread(() -> {
                if (finalItem != null) {
                    TextView brandText = findViewById(R.id.review_product_brand_text);
                    TextView nameText = findViewById(R.id.review_product_name_text);
                    TextView metaText = findViewById(R.id.review_product_meta_text);
                    ImageView productImage = findViewById(R.id.review_product_image);

                    if (brandText != null) {
                        brandText.setText(finalItem.brandName);
                    }

                    if (nameText != null) {
                        nameText.setText(finalItem.productName);
                    }

                    if (metaText != null) {
                        String orderCode = order != null ? order.ordersId : String.valueOf(orderId);
                        metaText.setText(
                                "Size: " + finalItem.sizeName
                                        + " · Màu: " + finalItem.colorName
                                        + " · Đơn hàng #" + orderCode
                        );
                    }

                    if (productImage != null) {
                        ImageLoader.load(finalItem.imageUrl, productImage, R.drawable.ic_shoe);
                    }
                }
            });
        });

        findViewById(R.id.review_photo_thumbnail).setVisibility(android.view.View.GONE);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        try {
                            getContentResolver().takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                        selectedImageUri = uri.toString();
                        View thumbnailContainer = findViewById(R.id.review_photo_thumbnail);
                        ImageView reviewPhotoImage = findViewById(R.id.review_photo_image);

                        if (thumbnailContainer != null && reviewPhotoImage != null) {
                            thumbnailContainer.setVisibility(android.view.View.VISIBLE);
                            reviewPhotoImage.setImageURI(uri);
                            reviewPhotoImage.clearColorFilter();
                            reviewPhotoImage.setImageTintList(null);
                        }
                    }
                }
        );

        findViewById(R.id.add_review_photo_button).setOnClickListener(v -> {
            pickImageLauncher.launch(new String[]{"image/*"});
        });

        stars = new ImageButton[]{
                findViewById(R.id.star_1),
                findViewById(R.id.star_2),
                findViewById(R.id.star_3),
                findViewById(R.id.star_4),
                findViewById(R.id.star_5)
        };

        ratingLabel = findViewById(R.id.rating_label);

        for (int i = 0; i < stars.length; i++) {
            final int selectedRating = i + 1;
            stars[i].setOnClickListener(v -> setRating(selectedRating));
        }

        setRating(rating);

        EditText reviewEditText = findViewById(R.id.review_edit_text);
        TextView characterCount = findViewById(R.id.review_char_count);

        updateCharacterCount(characterCount, reviewEditText.getText().length());

        reviewEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCharacterCount(characterCount, s.length());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        setupTag(findViewById(R.id.review_tag_comfortable));
        setupTag(findViewById(R.id.review_tag_true_to_size));
        setupTag(findViewById(R.id.review_tag_quality));
        setupTag(findViewById(R.id.review_tag_fast_delivery));

        findViewById(R.id.submit_review_button).setOnClickListener(v -> {
            String content = reviewEditText.getText().toString();
            saveReview(rating, content);
        });
    }

    private void setupTag(TextView tagView) {
        tagView.setSelected(false);

        tagView.setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
        });
    }

    private void setRating(int value) {
        rating = value;

        int filled = ContextCompat.getColor(this, R.color.star_filled);
        int empty = ContextCompat.getColor(this, R.color.star_empty);

        for (int i = 0; i < stars.length; i++) {
            stars[i].setColorFilter(i < rating ? filled : empty);
        }

        String[] labels = {
                "",
                getString(R.string.rating_1),
                getString(R.string.rating_2),
                getString(R.string.rating_3),
                getString(R.string.rating_4),
                getString(R.string.rating_5)
        };

        ratingLabel.setText(labels[rating]);
    }

    private void updateCharacterCount(TextView view, int length) {
        view.setText(length + "/500");
    }

    private void saveReview(int rating, String content) {
        if (productId == -1 || userId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder finalContent = new StringBuilder();

        TextView[] tags = {
                findViewById(R.id.review_tag_comfortable),
                findViewById(R.id.review_tag_true_to_size),
                findViewById(R.id.review_tag_quality),
                findViewById(R.id.review_tag_fast_delivery)
        };

        for (TextView tag : tags) {
            if (tag.isSelected()) {
                finalContent.append("[").append(tag.getText()).append("] ");
            }
        }

        finalContent.append(content);
        String savedContent = finalContent.toString().trim();

        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            ProductReview review = new ProductReview();

            review.productId = productId;
            review.userId = userId;

            if (orderId != -1) {
                review.orderId = orderId;
            }

            review.imageUrl = selectedImageUri;
            review.rating = rating;
            review.content = savedContent;

            SimpleDateFormat sdf =
                    new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault()
                    );

            review.createdAt = sdf.format(new Date());

            db.productDao().insertReview(review);

            runOnUiThread(() -> {
                Toast.makeText(
                        this,
                        getString(R.string.review_submitted_title),
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            });
        });
    }
}