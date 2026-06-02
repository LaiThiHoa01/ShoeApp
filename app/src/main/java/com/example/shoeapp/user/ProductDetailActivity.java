package com.example.shoeapp.user;

import android.os.Bundle;

import com.example.shoeapp.ui.BaseSoleStepActivity;
import com.example.shoeapp.ui.BottomNavHelper;
import com.example.shoeapp.R;

public class ProductDetailActivity extends BaseSoleStepActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        setupScreen(BottomNavHelper.TAG_SEARCH);
    }
}
