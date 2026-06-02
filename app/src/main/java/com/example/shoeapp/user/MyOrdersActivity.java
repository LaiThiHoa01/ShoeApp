package com.example.shoeapp.user;

import android.content.Intent;
import android.os.Bundle;

import com.example.shoeapp.ui.BaseSoleStepActivity;
import com.example.shoeapp.ui.BottomNavHelper;
import com.example.shoeapp.R;

public class MyOrdersActivity extends BaseSoleStepActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);
        setupScreen(BottomNavHelper.TAG_ORDERS);

        findViewById(R.id.rate_products_button).setOnClickListener(v ->
                startActivity(new Intent(this, ProductReviewActivity.class)));
        findViewById(R.id.order_detail_button).setOnClickListener(v ->
                startActivity(new Intent(this, OrderDetailActivity.class)));
    }
}
