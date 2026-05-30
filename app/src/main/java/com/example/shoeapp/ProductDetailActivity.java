package com.example.shoeapp;

import android.os.Bundle;

public class ProductDetailActivity extends BaseSoleStepActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        setupScreen(BottomNavHelper.TAG_SEARCH);
    }
}
