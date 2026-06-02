package com.example.shoeapp.user;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.shoeapp.ui.BaseSoleStepActivity;
import com.example.shoeapp.ui.BottomNavHelper;
import com.example.shoeapp.R;

public class ProductReviewActivity extends BaseSoleStepActivity {
    private ImageButton[] stars;
    private TextView ratingLabel;
    private int rating = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_review);
        setupScreen(BottomNavHelper.TAG_ORDERS);

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

        findViewById(R.id.submit_review_button).setOnClickListener(v ->
                Toast.makeText(this, getString(R.string.review_submitted_title), Toast.LENGTH_SHORT).show());
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
}
