package com.example.shoeapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.example.shoeapp.admin.adapter.AdminOrderAdapter;
import com.example.shoeapp.model.Order;
import com.example.shoeapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class AdminOrderManagementActivity extends AppCompatActivity
        implements AdminOrderAdapter.OnOrderActionListener {

    private RecyclerView        recyclerView;
    private EditText            searchInput;
    private TextView            filterAll, filterProcessing, filterShipped, filterDelivered;
    private TextView            statProcessingCount, statShippedCount, statDeliveredCount;
    private TextView            totalOrdersBadge;
    private BottomNavigationView bottomNav;

    private AdminOrderAdapter   adapter;
    private List<Order>         allOrders;
    private List<Order>         filteredOrders;

    private Order.Status currentStatus = null;
    private String currentSearch       = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_management);
        setupEdgeToEdge();
        bindViews();
        setupData();
        setupRecyclerView();
        setupSearch();
        setupFilterChips();
        setupBottomNav();
        updateStats();
    }

    private void setupEdgeToEdge() {
        androidx.activity.EdgeToEdge.enable(this);
        View root = findViewById(R.id.admin_orders_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void bindViews() {
        recyclerView         = findViewById(R.id.admin_orders_recycler);
        searchInput          = findViewById(R.id.admin_orders_search_input);
        filterAll            = findViewById(R.id.admin_ord_filter_all);
        filterProcessing     = findViewById(R.id.admin_ord_filter_processing);
        filterShipped        = findViewById(R.id.admin_ord_filter_shipped);
        filterDelivered      = findViewById(R.id.admin_ord_filter_delivered);
        statProcessingCount  = findViewById(R.id.admin_stat_processing_count);
        statShippedCount     = findViewById(R.id.admin_stat_shipped_count);
        statDeliveredCount   = findViewById(R.id.admin_stat_delivered_count);
        totalOrdersBadge     = findViewById(R.id.admin_orders_total_badge);
        bottomNav            = findViewById(R.id.admin_bottom_nav);
    }

    private void setupData() {
        allOrders = new ArrayList<>();
        allOrders.add(new Order("SS-10495", "Sarah Simpson", 649.98, 2,
                Order.Status.PROCESSING, "May 19, 2026"));
        allOrders.add(new Order("SS-10496", "Mike Johnson", 199.99, 1,
                Order.Status.SHIPPED, "May 20, 2026"));
        allOrders.add(new Order("SS-10497", "Emily Davis", 449.97, 2,
                Order.Status.DELIVERED, "May 18, 2026"));
        allOrders.add(new Order("SS-10498", "John Smith", 129.99, 1,
                Order.Status.SHIPPED, "May 21, 2026"));
        allOrders.add(new Order("SS-10499", "Lisa Chen", 79.99, 1,
                Order.Status.PROCESSING, "May 21, 2026"));
        allOrders.add(new Order("SS-10500", "Robert Wilson", 329.98, 2,
                Order.Status.DELIVERED, "May 17, 2026"));

        filteredOrders = new ArrayList<>(allOrders);
    }

    private void setupRecyclerView() {
        adapter = new AdminOrderAdapter(this, filteredOrders, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        int gapPx = (int) (12 * getResources().getDisplayMetrics().density);
        recyclerView.addItemDecoration(new AdminProductsActivity.SpaceItemDecoration(gapPx));
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                currentSearch = s.toString().trim().toLowerCase();
                applyFilters();
            }
        });
    }

    private void setupFilterChips() {
        filterAll.setOnClickListener(v          -> selectStatus(null, filterAll));
        filterProcessing.setOnClickListener(v   -> selectStatus(Order.Status.PROCESSING, filterProcessing));
        filterShipped.setOnClickListener(v      -> selectStatus(Order.Status.SHIPPED, filterShipped));
        filterDelivered.setOnClickListener(v    -> selectStatus(Order.Status.DELIVERED, filterDelivered));
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_orders);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_products) {
                startActivity(new Intent(this, AdminProductsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_categories) {
                startActivity(new Intent(this, AdminCategoriesActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_users) {
                startActivity(new Intent(this, UserManagementActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return id == R.id.nav_orders;
        });
    }

    private void selectStatus(Order.Status status, TextView selectedChip) {
        currentStatus = status;

        TextView[] chips = { filterAll, filterProcessing, filterShipped, filterDelivered };
        for (TextView chip : chips) {
            chip.setBackgroundResource(R.drawable.bg_admin_chip);
            chip.setTextColor(getColor(R.color.text_dark_tertiary));
        }
        selectedChip.setBackgroundResource(R.drawable.bg_admin_chip_selected);
        selectedChip.setTextColor(getColor(R.color.brand_white));

        applyFilters();
    }

    private void applyFilters() {
        filteredOrders.clear();

        for (Order o : allOrders) {
            boolean matchStatus = currentStatus == null || o.getStatus() == currentStatus;
            boolean matchSearch = currentSearch.isEmpty()
                    || o.getOrderId().toLowerCase().contains(currentSearch)
                    || o.getCustomerName().toLowerCase().contains(currentSearch);

            if (matchStatus && matchSearch) {
                filteredOrders.add(o);
            }
        }

        adapter.notifyDataSetChanged();
    }


    private void updateStats() {
        int processing = 0, shipped = 0, delivered = 0;

        for (Order order : allOrders) {
            switch (order.getStatus()) {
                case PROCESSING: processing++; break;
                case SHIPPED:    shipped++;    break;
                case DELIVERED:  delivered++;  break;
            }
        }

        statProcessingCount.setText(String.valueOf(processing));
        statShippedCount.setText(String.valueOf(shipped));
        statDeliveredCount.setText(String.valueOf(delivered));
        totalOrdersBadge.setText(String.format("%d orders", allOrders.size()));
    }

    // ── AdminOrderAdapter.OnOrderActionListener ───────────────────────────────

    @Override
    public void onViewDetailsClick(Order order, int position) {
        Toast.makeText(this,
                "Chi tiết đơn: " + order.getOrderId(),
                Toast.LENGTH_SHORT).show();
        // TODO: startActivity(new Intent(this, AdminOrderDetailActivity.class)
        //           .putExtra("order_id", order.getOrderId()));
    }

    @Override
    public void onMarkShippedClick(Order order, int position) {
        order.setStatus(Order.Status.SHIPPED);
        adapter.notifyItemChanged(position);
        updateStats();
        applyFilters();
        Toast.makeText(this,
                "Cập nhật: " + order.getOrderId() + " → Đã gửi",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkDeliveredClick(Order order, int position) {
        order.setStatus(Order.Status.DELIVERED);
        adapter.notifyItemChanged(position);
        updateStats();
        applyFilters();
        Toast.makeText(this,
                "Cập nhật: " + order.getOrderId() + " → Đã giao",
                Toast.LENGTH_SHORT).show();
    }
}

