package com.example.shoeapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.entity.Product;
import com.example.shoeapp.data.model.OrderWithUser;
import com.example.shoeapp.user.MainActivity;
import com.example.shoeapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Dashboard Admin - Đã sửa lỗi StatusBar và Navigation Menu.
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private AppDatabase db;
    private final DecimalFormat currencyFormat = new DecimalFormat("#,### ₫");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Kích hoạt EdgeToEdge TRƯỚC super.onCreate
        androidx.activity.EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = AppDatabase.getDatabase(this);

        // 2. Xử lý vùng an toàn (Insets) để không bị đè bởi StatusBar và NavBar hệ thống
        View root = findViewById(R.id.admin_dashboard_root);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                // Pad toàn bộ Root Layout để đẩy Header xuống và Menu lên
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return WindowInsetsCompat.CONSUMED;
            });
        }

        setupNavigation();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDashboardData();
    }

    private void setupNavigation() {
        // Tìm trực tiếp BottomNavigationView từ ID gốc trong view_admin_bottom_nav.xml
        BottomNavigationView bottomNav = findViewById(R.id.admin_bottom_nav);

        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_dashboard);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_dashboard) return true;
                
                Intent intent = null;
                if (id == R.id.nav_users) {
                    intent = new Intent(this, UserManagementActivity.class);
                } else if (id == R.id.nav_categories) {
                    intent = new Intent(this, AdminCategoriesActivity.class);
                } else if (id == R.id.nav_products) {
                    intent = new Intent(this, AdminProductsActivity.class);
                } else if (id == R.id.nav_orders) {
                    intent = new Intent(this, AdminOrderManagementActivity.class);
                }
                
                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(0, 0); // Hiệu ứng mượt mà
                    finish();
                    return true;
                }
                return false;
            });
        }
    }

    private void setupClickListeners() {
        View btnAllOrders = findViewById(R.id.btn_view_all_orders);
        if (btnAllOrders != null) {
            btnAllOrders.setOnClickListener(v -> startActivity(new Intent(this, AdminOrderManagementActivity.class)));
        }

        View btnAllProducts = findViewById(R.id.btn_view_all_products);
        if (btnAllProducts != null) {
            btnAllProducts.setOnClickListener(v -> startActivity(new Intent(this, AdminProductsActivity.class)));
        }

        View profile = findViewById(R.id.profile_image_bg);
        if (profile != null) {
            profile.setOnClickListener(v -> {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            });
        }
    }

    private void refreshDashboardData() {
        try {
            int orderCount = db.orderDao().countOrders();
            int productCount = db.productDao().countProducts();
            int customerCount = db.userDao().countCustomers();
            
            Double totalRevValue = db.orderDao().getTotalRevenue();
            double totalRevenue = (totalRevValue != null) ? totalRevValue : 0.0;

            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
            Double todayRevValue = db.orderDao().getRevenueByDate(today);
            double todayRevenue = (todayRevValue != null) ? todayRevValue : 0.0;

            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.DATE, -1);
            String yesterday = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.getTime());
            Double yesterdayRevValue = db.orderDao().getRevenueByDate(yesterday);
            double yesterdayRevenue = (yesterdayRevValue != null) ? yesterdayRevValue : 0.0;

            TextView tvTodayRevenue = findViewById(R.id.revenue_amount);
            if (tvTodayRevenue != null) tvTodayRevenue.setText(currencyFormat.format(todayRevenue));

            TextView tvRevenueTrend = findViewById(R.id.tv_revenue_trend);
            if (tvRevenueTrend != null) {
                String trendText;
                if (yesterdayRevenue == 0.0) {
                    if (todayRevenue > 0.0) {
                        trendText = "+100% so với hôm qua";
                    } else {
                        trendText = "0% so với hôm qua";
                    }
                } else {
                    double diffPercent = ((todayRevenue - yesterdayRevenue) / yesterdayRevenue) * 100.0;
                    if (diffPercent >= 0.0) {
                        trendText = String.format(Locale.US, "+%.1f%% so với hôm qua", diffPercent);
                    } else {
                        trendText = String.format(Locale.US, "%.1f%% so với hôm qua", diffPercent);
                    }
                }
                tvRevenueTrend.setText(trendText);
            }

            TextView tvOrders = findViewById(R.id.tv_stat_orders_value);
            if (tvOrders != null) tvOrders.setText(String.valueOf(orderCount));

            TextView tvProducts = findViewById(R.id.tv_stat_products_value);
            if (tvProducts != null) tvProducts.setText(String.valueOf(productCount));

            TextView tvCustomers = findViewById(R.id.tv_stat_customers_value);
            if (tvCustomers != null) tvCustomers.setText(String.valueOf(customerCount));
            
            TextView tvStatRev = findViewById(R.id.tv_stat_revenue_value);
            if (tvStatRev != null) {
                if (totalRevenue >= 1000000) {
                    tvStatRev.setText(String.format(Locale.getDefault(), "%.1fM", totalRevenue / 1000000.0));
                } else {
                    tvStatRev.setText(currencyFormat.format(totalRevenue));
                }
            }

            loadRecentOrdersList();
            loadTopProductsList();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadRecentOrdersList() {
        LinearLayout container = findViewById(R.id.layout_recent_orders);
        if (container == null) return;
        
        container.removeAllViews();
        List<OrderWithUser> recentOrders = db.orderDao().getRecentOrdersWithUser(4);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < recentOrders.size(); i++) {
            OrderWithUser item = recentOrders.get(i);
            View itemView = inflater.inflate(R.layout.item_admin_recent_order, container, false);

            TextView tvOrderId = itemView.findViewById(R.id.tv_order_id);
            TextView tvCustName = itemView.findViewById(R.id.tv_customer_name);
            TextView tvPrice = itemView.findViewById(R.id.tv_order_price);
            TextView tvStatus = itemView.findViewById(R.id.tv_order_status);

            if (tvOrderId != null) tvOrderId.setText(item.order.ordersId);
            if (tvCustName != null) tvCustName.setText(item.userName);
            if (tvPrice != null) tvPrice.setText(currencyFormat.format(item.order.grandTotal));
            
            if (tvStatus != null) {
                String status = item.order.orderStatus;
                if ("DELIVERED".equals(status)) {
                    tvStatus.setText(getString(R.string.status_delivered));
                    tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_success));
                    tvStatus.setBackgroundResource(R.drawable.bg_admin_status_delivered);
                } else if ("SHIPPED".equals(status)) {
                    tvStatus.setText(getString(R.string.status_shipped));
                    tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_info));
                    tvStatus.setBackgroundResource(R.drawable.bg_admin_status_shipped);
                } else if ("CANCELLED".equals(status)) {
                    tvStatus.setText(getString(R.string.status_cancelled));
                    tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_error));
                    tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
                } else { // PENDING, PROCESSING
                    tvStatus.setText(getString(R.string.status_processing));
                    tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_warning));
                    tvStatus.setBackgroundResource(R.drawable.bg_admin_status_processing);
                }
            }
            container.addView(itemView);
        }
    }

    private void loadTopProductsList() {
        LinearLayout container = findViewById(R.id.layout_top_products);
        if (container == null) return;
        
        container.removeAllViews();
        List<Product> products = db.productDao().getTopSellingProducts(5);
        if (products.isEmpty()) products = db.productDao().getAllProducts();

        LayoutInflater inflater = LayoutInflater.from(this);
        int limit = Math.min(products.size(), 5);

        for (int i = 0; i < limit; i++) {
            Product p = products.get(i);
            View itemView = inflater.inflate(R.layout.item_admin_top_product, container, false);

            android.widget.ImageView ivImage = itemView.findViewById(R.id.iv_product_image);
            TextView tvName = itemView.findViewById(R.id.tv_product_name);
            TextView tvPrice = itemView.findViewById(R.id.tv_product_price);
            TextView tvStock = itemView.findViewById(R.id.tv_product_stock);

            if (tvName != null) tvName.setText(p.name);
            if (tvPrice != null) tvPrice.setText(currencyFormat.format(p.price));
            if (tvStock != null) {
                tvStock.setText(getString(R.string.admin_stock_format, db.productDao().getProductStock(p.id)));
            }
            if (ivImage != null) {
                com.example.shoeapp.data.entity.ProductImg thumbnail = db.productDao().getThumbnail(p.id);
                String imgUrl = (thumbnail != null) ? thumbnail.imgUrl : "";
                com.example.shoeapp.user.ImageLoader.load(imgUrl, ivImage, R.drawable.ic_shoe);
            }
            container.addView(itemView);
        }
    }
}
