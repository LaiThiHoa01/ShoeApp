package com.example.shoeapp;

import android.os.Bundle;

public class MainActivity extends BaseSoleStepActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setupScreen(BottomNavHelper.TAG_HOME);
    }
}
