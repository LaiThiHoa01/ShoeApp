package com.example.shoeapp;

import android.os.Bundle;

public class CatalogActivity extends BaseSoleStepActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        setupScreen(BottomNavHelper.TAG_SEARCH);
    }
}
