package com.example.shoeapp.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.R;
import com.example.shoeapp.model.Product;
import com.example.shoeapp.ui.BaseSoleStepActivity;
import com.example.shoeapp.ui.BottomNavHelper;
import com.example.shoeapp.user.adapter.ClientProductAdapter;
import com.example.shoeapp.user.adapter.ProductGridSpacingDecoration;

public class CatalogActivity extends BaseSoleStepActivity {
    private ClientProductRepository productRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        productRepository = new ClientProductRepository(this);
        productRepository.ensureSeedData();

        setupScreen(BottomNavHelper.TAG_SEARCH);
        setupProductGrid();
    }

    private void setupProductGrid() {
        RecyclerView productGrid = findViewById(R.id.catalog_product_grid);
        java.util.List<Product> products = productRepository.getAllProducts();
        productGrid.setLayoutManager(new GridLayoutManager(this, 2));
        productGrid.setAdapter(new ClientProductAdapter(this, products, this::openProductDetail));
        productGrid.addItemDecoration(new ProductGridSpacingDecoration(
                getResources().getDimensionPixelSize(R.dimen.space_6),
                getResources().getDimensionPixelSize(R.dimen.space_8)));
        ((TextView) findViewById(R.id.catalog_count_text)).setText(products.size() + " sản phẩm");
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }
}
