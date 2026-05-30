package com.example.shoeapp;

import android.os.Bundle;
import android.content.Intent;

public class CatalogActivity extends BaseSoleStepActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        setupScreen(BottomNavHelper.TAG_SEARCH);

        findViewById(R.id.catalog_product_grid).setOnClickListener(v ->
                startActivity(new Intent(this, ProductDetailActivity.class)));
    }
}
