package com.example.shoeapp;

import android.os.Bundle;

public class CartActivity extends BaseSoleStepActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        setupScreen(BottomNavHelper.TAG_CART);
    }
}
