package com.example.shoeapp;

import android.content.Intent;
import android.os.Bundle;

public class OrderDetailActivity extends BaseSoleStepActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        setupScreen(BottomNavHelper.TAG_ORDERS);

        findViewById(R.id.rate_products_button).setOnClickListener(v ->
                startActivity(new Intent(this, ProductReviewActivity.class)));
    }
}
