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
import com.example.shoeapp.data.AppDatabase;
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

    private RecyclerView         recyclerView;
    private EditText             searchInput;
    private TextView             filterAll, filterProcessing, filterShipped, filterDelivered;
    private TextView             filterTimeAll, filterTimeToday, filterTimeWeek, filterTimeMonth;
    private TextView             statProcessingCount, statShippedCount, statDeliveredCount;
    private TextView             totalOrdersBadge;
    private BottomNavigationView bottomNav;

    private AdminOrderAdapter    adapter;
    private List<Order>          allOrders;
    private List<Order>          filteredOrders;
    private AppDatabase          db;

    private List<com.example.shoeapp.data.entity.Order> dbOrders = new ArrayList<>();

    private Order.Status currentStatus = null;
    private String       currentTimeFilter = "ALL"; // "ALL", "TODAY", "WEEK", "MONTH"
    private String       currentSearch = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_management);
        db = AppDatabase.getDatabase(this);
        setupEdgeToEdge();
        bindViews();
        loadFromDb();
        setupRecyclerView();
        setupSearch();
        setupFilterChips();
        setupTimeFilterChips();
        setupBottomNav();
        updateStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFromDb();
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
        recyclerView        = findViewById(R.id.admin_orders_recycler);
        searchInput         = findViewById(R.id.admin_orders_search_input);
        filterAll           = findViewById(R.id.admin_ord_filter_all);
        filterProcessing    = findViewById(R.id.admin_ord_filter_processing);
        filterShipped       = findViewById(R.id.admin_ord_filter_shipped);
        filterDelivered     = findViewById(R.id.admin_ord_filter_delivered);
        filterTimeAll       = findViewById(R.id.admin_ord_time_all);
        filterTimeToday     = findViewById(R.id.admin_ord_time_today);
        filterTimeWeek      = findViewById(R.id.admin_ord_time_week);
        filterTimeMonth     = findViewById(R.id.admin_ord_time_month);
        statProcessingCount = findViewById(R.id.admin_stat_processing_count);
        statShippedCount    = findViewById(R.id.admin_stat_shipped_count);
        statDeliveredCount  = findViewById(R.id.admin_stat_delivered_count);
        totalOrdersBadge    = findViewById(R.id.admin_orders_total_badge);
        bottomNav           = findViewById(R.id.admin_bottom_nav);
    }

    private void loadFromDb() {
        dbOrders  = db.orderDao().getAllOrders();
        if (allOrders == null) {
            allOrders = new ArrayList<>();
        } else {
            allOrders.clear();
        }

        for (com.example.shoeapp.data.entity.Order entity : dbOrders) {
            com.example.shoeapp.data.entity.User user =
                    db.userDao().getUserById(entity.userId);
            String customerName = (user != null && user.fullName != null)
                    ? user.fullName : "User #" + entity.userId;

            int itemCount    = db.orderDao().getDetailsByOrder(entity.id).size();
            Order.Status status = convertStatus(entity.orderStatus);
            String date      = entity.createdAt != null
                    ? entity.createdAt.substring(0, Math.min(10, entity.createdAt.length())) : "—";

            allOrders.add(new Order(
                    entity.ordersId != null ? entity.ordersId : "#" + entity.id,
                    customerName,
                    entity.grandTotal != null ? entity.grandTotal : 0.0,
                    itemCount,
                    status,
                    date
            ));
        }

        if (filteredOrders == null) {
            filteredOrders = new ArrayList<>(allOrders);
        } else {
            filteredOrders.clear();
            filteredOrders.addAll(allOrders);
        }
        if (adapter != null) applyFilters();
    }

    private Order.Status convertStatus(String s) {
        if (s == null) return Order.Status.PROCESSING;
        switch (s) {
            case "SHIPPED":   return Order.Status.SHIPPED;
            case "DELIVERED":
            case "COMPLETED": return Order.Status.DELIVERED;
            case "CANCELLED": return Order.Status.CANCELLED;
            default:          return Order.Status.PROCESSING;
        }
    }

    private void setupRecyclerView() {
        adapter = new AdminOrderAdapter(this, this);
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
        filterAll.setOnClickListener(v        -> selectStatus(null,                    filterAll));
        filterProcessing.setOnClickListener(v -> selectStatus(Order.Status.PROCESSING, filterProcessing));
        filterShipped.setOnClickListener(v    -> selectStatus(Order.Status.SHIPPED,    filterShipped));
        filterDelivered.setOnClickListener(v  -> selectStatus(Order.Status.DELIVERED,  filterDelivered));
    }

    private void setupTimeFilterChips() {
        filterTimeAll.setOnClickListener(v   -> selectTimeFilter("ALL",   filterTimeAll));
        filterTimeToday.setOnClickListener(v -> selectTimeFilter("TODAY", filterTimeToday));
        filterTimeWeek.setOnClickListener(v  -> selectTimeFilter("WEEK",  filterTimeWeek));
        filterTimeMonth.setOnClickListener(v -> selectTimeFilter("MONTH", filterTimeMonth));
    }

    private void selectTimeFilter(String timeFilter, TextView selectedChip) {
        currentTimeFilter = timeFilter;
        TextView[] chips = { filterTimeAll, filterTimeToday, filterTimeWeek, filterTimeMonth };
        for (TextView chip : chips) {
            chip.setBackgroundResource(R.drawable.bg_admin_chip);
            chip.setTextColor(getColor(R.color.text_dark_tertiary));
        }
        selectedChip.setBackgroundResource(R.drawable.bg_admin_chip_selected);
        selectedChip.setTextColor(getColor(R.color.brand_white));
        applyFilters();
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_orders);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
                overridePendingTransition(0, 0); finish(); return true;
            } else if (id == R.id.nav_users) {
                startActivity(new Intent(this, UserManagementActivity.class));
                overridePendingTransition(0, 0); finish(); return true;
            } else if (id == R.id.nav_categories) {
                startActivity(new Intent(this, AdminCategoriesActivity.class));
                overridePendingTransition(0, 0); finish(); return true;
            } else if (id == R.id.nav_products) {
                startActivity(new Intent(this, AdminProductsActivity.class));
                overridePendingTransition(0, 0); finish(); return true;
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
            boolean matchTime = matchesTimeFilter(o.getDate());
            if (matchStatus && matchSearch && matchTime) filteredOrders.add(o);
        }
        adapter.submitList(new ArrayList<>(filteredOrders));
    }

    private boolean matchesTimeFilter(String dateStr) {
        if (currentTimeFilter.equals("ALL")) return true;
        if (dateStr == null || dateStr.equals("—")) return false;

        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
            java.util.Date orderDate = sdf.parse(dateStr);
            if (orderDate == null) return false;

            java.util.Calendar calOrder = java.util.Calendar.getInstance();
            calOrder.setTime(orderDate);

            java.util.Calendar calToday = java.util.Calendar.getInstance();
            // Reset time for precise day comparison
            calToday.set(java.util.Calendar.HOUR_OF_DAY, 0);
            calToday.set(java.util.Calendar.MINUTE, 0);
            calToday.set(java.util.Calendar.SECOND, 0);
            calToday.set(java.util.Calendar.MILLISECOND, 0);

            if (currentTimeFilter.equals("TODAY")) {
                return calOrder.get(java.util.Calendar.YEAR) == calToday.get(java.util.Calendar.YEAR)
                        && calOrder.get(java.util.Calendar.DAY_OF_YEAR) == calToday.get(java.util.Calendar.DAY_OF_YEAR);
            } else if (currentTimeFilter.equals("WEEK")) {
                return calOrder.get(java.util.Calendar.YEAR) == calToday.get(java.util.Calendar.YEAR)
                        && calOrder.get(java.util.Calendar.WEEK_OF_YEAR) == calToday.get(java.util.Calendar.WEEK_OF_YEAR);
            } else if (currentTimeFilter.equals("MONTH")) {
                return calOrder.get(java.util.Calendar.YEAR) == calToday.get(java.util.Calendar.YEAR)
                        && calOrder.get(java.util.Calendar.MONTH) == calToday.get(java.util.Calendar.MONTH);
            }
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void updateStats() {
        int processing = 0, shipped = 0, delivered = 0;
        for (Order order : allOrders) {
            switch (order.getStatus()) {
                case PROCESSING: processing++; break;
                case SHIPPED:    shipped++;    break;
                case DELIVERED:  delivered++;  break;
                case CANCELLED:  break;
            }
        }
        statProcessingCount.setText(String.valueOf(processing));
        statShippedCount.setText(String.valueOf(shipped));
        statDeliveredCount.setText(String.valueOf(delivered));
        totalOrdersBadge.setText(String.format("%d đơn hàng", allOrders.size()));
    }

    @Override
    public void onViewDetailsClick(Order order, int position) {
        int dbId = -1;
        for (com.example.shoeapp.data.entity.Order entity : dbOrders) {
            String eId = entity.ordersId != null ? entity.ordersId : "#" + entity.id;
            if (eId.equals(order.getOrderId())) {
                dbId = entity.id;
                break;
            }
        }
        if (dbId != -1) {
            Intent intent = new Intent(this, AdminOrderDetailActivity.class);
            intent.putExtra(AdminOrderDetailActivity.EXTRA_ORDER_ID, dbId);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Không tìm thấy mã đơn: " + order.getOrderId(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMarkShippedClick(Order order, int position) {
        order.setStatus(Order.Status.SHIPPED);
        updateOrderStatusInDb(order.getOrderId(), "SHIPPED");
        updateStats();
        applyFilters();
        Toast.makeText(this, "Cập nhật: " + order.getOrderId() + " → Đã gửi", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkDeliveredClick(Order order, int position) {
        order.setStatus(Order.Status.DELIVERED);
        updateOrderStatusInDb(order.getOrderId(), "DELIVERED");
        updateStats();
        applyFilters();
        Toast.makeText(this, "Cập nhật: " + order.getOrderId() + " → Đã giao", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancelOrderClick(Order order, int position) {
        order.setStatus(Order.Status.CANCELLED);
        updateOrderStatusInDb(order.getOrderId(), "CANCELLED");
        updateStats();
        applyFilters();
        Toast.makeText(this, "Đã hủy đơn hàng: " + order.getOrderId(), Toast.LENGTH_SHORT).show();
    }

    private void updateOrderStatusInDb(String orderId, String newStatus) {
        for (com.example.shoeapp.data.entity.Order entity : dbOrders) {
            String eId = entity.ordersId != null ? entity.ordersId : "#" + entity.id;
            if (eId.equals(orderId)) {
                entity.orderStatus = newStatus;
                db.orderDao().update(entity);
                break;
            }
        }
    }
}
