package com.example.shoeapp.user;

import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.R;
import com.example.shoeapp.admin.AdminDashboardActivity;
import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.model.Product;
import com.example.shoeapp.ui.BaseSoleStepActivity;
import com.example.shoeapp.ui.BottomNavHelper;
import com.example.shoeapp.user.adapter.ClientProductAdapter;
import com.example.shoeapp.user.adapter.ProductGridSpacingDecoration;

public class MainActivity extends BaseSoleStepActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        AppDatabase.getDatabase(this).userDao().getAllUsers();

        setupScreen(BottomNavHelper.TAG_HOME);
        setupProductGrid();

        findViewById(R.id.btn_admin_access).setOnClickListener(v ->
                startActivity(new Intent(this, AdminDashboardActivity.class)));
    }

    private void setupProductGrid() {
        RecyclerView productGrid = findViewById(R.id.home_product_grid);
        productGrid.setLayoutManager(new GridLayoutManager(this, 2));
        productGrid.setAdapter(new ClientProductAdapter(this, ClientProductSamples.featured(), this::openProductDetail));
        productGrid.addItemDecoration(new ProductGridSpacingDecoration(
                getResources().getDimensionPixelSize(R.dimen.space_6),
                getResources().getDimensionPixelSize(R.dimen.space_8)));
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }
}
