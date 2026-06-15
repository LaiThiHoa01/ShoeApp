package com.example.shoeapp.user;

import android.content.Intent;
import android.os.Bundle;

import com.example.shoeapp.R;
import com.example.shoeapp.admin.AdminDashboardActivity;
import com.example.shoeapp.ui.BaseSoleStepActivity;
import com.example.shoeapp.ui.BottomNavHelper;

import com.example.shoeapp.data.AppDatabase;

public class MainActivity extends BaseSoleStepActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        // gọi query để room sinh ra file db
        AppDatabase.getDatabase(this).userDao().getAllUsers();
        
        setupScreen(BottomNavHelper.TAG_HOME);

        findViewById(R.id.home_product_grid).setOnClickListener(v ->
                startActivity(new Intent(this, ProductDetailActivity.class)));

        findViewById(R.id.btn_admin_access).setOnClickListener(v ->
                startActivity(new Intent(this, AdminDashboardActivity.class)));
    }
}
