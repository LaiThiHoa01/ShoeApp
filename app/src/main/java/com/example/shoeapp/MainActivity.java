package com.example.shoeapp;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends BaseSoleStepActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setupScreen(BottomNavHelper.TAG_HOME);

        findViewById(R.id.home_product_grid).setOnClickListener(v ->
                startActivity(new Intent(this, ProductDetailActivity.class)));
    }
}
