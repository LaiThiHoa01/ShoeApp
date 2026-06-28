package com.example.shoeapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.R;
import com.example.shoeapp.admin.adapter.AdminPromotionAdapter;
import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.dao.ProductDao;
import com.example.shoeapp.data.entity.Promotion;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.concurrent.Executors;

public class AdminPromotionActivity extends AppCompatActivity {

    private RecyclerView rvPromotions;
    private View layoutNoData;
    private AdminPromotionAdapter adapter;
    private ProductDao productDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_promotion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.admin_promotion_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        productDao = AppDatabase.getDatabase(this).productDao();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        rvPromotions = findViewById(R.id.rv_promotions);
        layoutNoData = findViewById(R.id.layout_no_data);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_promotion);

        rvPromotions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminPromotionAdapter(promotion -> {
             Intent intent = new Intent(this, AdminAddPromotionActivity.class);
             intent.putExtra("promotion_id", promotion.id);
             startActivity(intent);
        });
        rvPromotions.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
             Intent intent = new Intent(this, AdminAddPromotionActivity.class);
             startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPromotions();
    }

    private void loadPromotions() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Promotion> promotions = productDao.getAllPromotions();
            runOnUiThread(() -> {
                if (promotions == null || promotions.isEmpty()) {
                    layoutNoData.setVisibility(View.VISIBLE);
                    rvPromotions.setVisibility(View.GONE);
                } else {
                    layoutNoData.setVisibility(View.GONE);
                    rvPromotions.setVisibility(View.VISIBLE);
                    adapter.submitList(promotions);
                }
            });
        });
    }
}
