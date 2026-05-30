package com.example.shoeapp;

import android.content.Intent;
import android.os.Bundle;

public class CartActivity extends BaseSoleStepActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        setupScreen(BottomNavHelper.TAG_CART);

        findViewById(R.id.checkout_button).setOnClickListener(v ->
                startActivity(new Intent(this, CheckoutActivity.class)));
    }
}
