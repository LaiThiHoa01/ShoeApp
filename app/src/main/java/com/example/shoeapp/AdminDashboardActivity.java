package com.example.shoeapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        if (getWindow() != null) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.bg_primary));
        }
    }
}
