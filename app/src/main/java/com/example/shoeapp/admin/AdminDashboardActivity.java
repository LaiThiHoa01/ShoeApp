package com.example.shoeapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shoeapp.authentication.LoginActivity;
import com.example.shoeapp.authentication.SessionManager;
import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.entity.Product;
import com.example.shoeapp.data.model.OrderWithUser;
import com.example.shoeapp.user.MainActivity;
import com.example.shoeapp.R;
import com.example.shoeapp.data.repo.ProductRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.widget.Toast;

public class AdminDashboardActivity extends BaseAdminActivity {

    private AppDatabase db;
    private final DecimalFormat currencyFormat = new DecimalFormat("#,### ₫");

    private String startDateFilter;
    private String endDateFilter;
    private com.google.android.material.button.MaterialButton btnStartDate, btnEndDate;
    private LinearLayout layoutChartBars;
    private TextView tvChartNoData;
    private final SimpleDateFormat dateFormatDb = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat dateFormatDisplay = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

    private synchronized Date parseDbDate(String dateStr) throws java.text.ParseException {
        return dateFormatDb.parse(dateStr);
    }

    private synchronized String formatDbDate(Date date) {
        return dateFormatDb.format(date);
    }

    private synchronized String formatDisplayDate(Date date) {
        return dateFormatDisplay.format(date);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Kích hoạt EdgeToEdge TRƯỚC super.onCreate
        androidx.activity.EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = AppDatabase.getDatabase(this);

        new ProductRepository(this).ensureSeedData();

        View root = findViewById(R.id.admin_dashboard_root);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return WindowInsetsCompat.CONSUMED;
            });
        }

        java.util.Calendar cal = java.util.Calendar.getInstance();
        endDateFilter = formatDbDate(cal.getTime());
        cal.add(java.util.Calendar.DATE, -6);
        startDateFilter = formatDbDate(cal.getTime());

        btnStartDate = findViewById(R.id.btn_start_date);
        btnEndDate = findViewById(R.id.btn_end_date);
        layoutChartBars = findViewById(R.id.layout_chart_bars);
        tvChartNoData = findViewById(R.id.tv_chart_no_data);

        updateFilterButtonsLabel();

        if (btnStartDate != null) {
            btnStartDate.setOnClickListener(v -> showDatePicker(true));
        }
        if (btnEndDate != null) {
            btnEndDate.setOnClickListener(v -> showDatePicker(false));
        }

        setupNavigation();
        setupClickListeners();

    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.admin_bottom_nav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_dashboard);
        }
        refreshDashboardData();
    }

    private void setupNavigation() {
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
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            });
        }

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
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

        View btnPromotions = findViewById(R.id.btn_manage_promotions);
        if (btnPromotions != null) {
            btnPromotions.setOnClickListener(v -> startActivity(new Intent(this, AdminPromotionActivity.class)));
        }

        View btnLogout = findViewById(R.id.btn_admin_logout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Đăng xuất")
                        .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản Admin?")
                        .setPositiveButton("Đăng xuất", (dialog, which) -> {
                            SessionManager.clear(this);
                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });
        }
    }

    private void refreshDashboardData() {
        new Thread(() -> {
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

                List<OrderWithUser> recentOrders = db.orderDao().getRecentOrdersWithUser(5);
                if (recentOrders == null) {
                    recentOrders = new ArrayList<>();
                }

                List<Product> products = db.productDao().getTopSellingProducts(5);
                if (products == null || products.isEmpty()) {
                    products = db.productDao().getAllProducts();
                }
                if (products == null) {
                    products = new ArrayList<>();
                }
                
                List<Integer> topStocks = new ArrayList<>();
                List<String> topImageUrls = new ArrayList<>();
                int limit = Math.min(products.size(), 5);
                for (int i = 0; i < limit; i++) {
                    Product p = products.get(i);
                    topStocks.add(db.productDao().getProductStock(p.id));
                    com.example.shoeapp.data.entity.ProductImg thumbnail = db.productDao().getThumbnail(p.id);
                    topImageUrls.add(thumbnail != null ? thumbnail.imgUrl : "");
                }

                final List<Product> finalProducts = products;
                final List<OrderWithUser> finalRecentOrders = recentOrders;
                runOnUiThread(() -> {
                    TextView tvTodayRevenue = findViewById(R.id.revenue_amount);
                    if (tvTodayRevenue != null) tvTodayRevenue.setText(currencyFormat.format(todayRevenue));

                    TextView tvRevenueTrend = findViewById(R.id.tv_revenue_trend);
                    if (tvRevenueTrend != null) {
                        String trendText;
                        if (yesterdayRevenue == 0.0) {
                            trendText = todayRevenue > 0.0 ? "+100% so với hôm qua" : "0% so với hôm qua";
                        } else {
                            double diffPercent = ((todayRevenue - yesterdayRevenue) / yesterdayRevenue) * 100.0;
                            trendText = String.format(Locale.US, diffPercent >= 0.0 ? "+%.1f%% so với hôm qua" : "%.1f%% so với hôm qua", diffPercent);
                        }
                        tvRevenueTrend.setText(trendText);
                    }

                    TextView tvOrders = findViewById(R.id.tv_stat_orders_value);
                    if (tvOrders != null) tvOrders.setText(String.valueOf(orderCount));

                    TextView tvProductsView = findViewById(R.id.tv_stat_products_value);
                    if (tvProductsView != null) tvProductsView.setText(String.valueOf(productCount));

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

                    LinearLayout containerOrders = findViewById(R.id.layout_recent_orders);
                    if (containerOrders != null && finalRecentOrders != null) {
                        containerOrders.removeAllViews();
                        LayoutInflater inflater = LayoutInflater.from(AdminDashboardActivity.this);
                        for (OrderWithUser item : finalRecentOrders) {
                            if (item == null || item.order == null) continue;
                            View itemView = inflater.inflate(R.layout.item_admin_recent_order, containerOrders, false);
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
                                    tvStatus.setTextColor(ContextCompat.getColor(AdminDashboardActivity.this, R.color.status_success));
                                    tvStatus.setBackgroundResource(R.drawable.bg_admin_status_delivered);
                                } else if ("SHIPPED".equals(status)) {
                                    tvStatus.setText(getString(R.string.status_shipped));
                                    tvStatus.setTextColor(ContextCompat.getColor(AdminDashboardActivity.this, R.color.status_info));
                                    tvStatus.setBackgroundResource(R.drawable.bg_admin_status_shipped);
                                } else if ("CANCELLED".equals(status)) {
                                    tvStatus.setText(getString(R.string.status_cancelled));
                                    tvStatus.setTextColor(ContextCompat.getColor(AdminDashboardActivity.this, R.color.status_error));
                                    tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
                                } else {
                                    tvStatus.setText(getString(R.string.status_processing));
                                    tvStatus.setTextColor(ContextCompat.getColor(AdminDashboardActivity.this, R.color.status_warning));
                                    tvStatus.setBackgroundResource(R.drawable.bg_admin_status_processing);
                                }
                            }
                            itemView.setOnClickListener(v -> {
                                Intent intent = new Intent(AdminDashboardActivity.this, AdminOrderDetailActivity.class);
                                intent.putExtra(AdminOrderDetailActivity.EXTRA_ORDER_ID, item.order.id);
                                startActivity(intent);
                            });
                            containerOrders.addView(itemView);
                        }
                    }

                    LinearLayout containerProducts = findViewById(R.id.layout_top_products);
                    if (containerProducts != null && finalProducts != null) {
                        containerProducts.removeAllViews();
                        LayoutInflater inflater = LayoutInflater.from(AdminDashboardActivity.this);
                        int finalLimit = Math.min(finalProducts.size(), 5);
                        for (int i = 0; i < finalLimit; i++) {
                            Product p = finalProducts.get(i);
                            if (p == null) continue;
                            View itemView = inflater.inflate(R.layout.item_admin_top_product, containerProducts, false);
                            android.widget.ImageView ivImage = itemView.findViewById(R.id.iv_product_image);
                            TextView tvName = itemView.findViewById(R.id.tv_product_name);
                            TextView tvPrice = itemView.findViewById(R.id.tv_product_price);
                            TextView tvStock = itemView.findViewById(R.id.tv_product_stock);

                            if (tvName != null) tvName.setText(p.name);
                            if (tvPrice != null) tvPrice.setText(currencyFormat.format(p.price));
                            if (tvStock != null && i < topStocks.size()) {
                                tvStock.setText(getString(R.string.admin_stock_format, topStocks.get(i)));
                            }
                            if (ivImage != null && i < topImageUrls.size()) {
                                com.example.shoeapp.user.ImageLoader.load(topImageUrls.get(i), ivImage, R.drawable.ic_shoe);
                            }
                            containerProducts.addView(itemView);
                        }
                    }
                });

                loadRevenueChart();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateFilterButtonsLabel() {
        try {
            Date start = parseDbDate(startDateFilter);
            Date end = parseDbDate(endDateFilter);
            if (btnStartDate != null && start != null) {
                btnStartDate.setText("Từ: " + formatDisplayDate(start));
            }
            if (btnEndDate != null && end != null) {
                btnEndDate.setText("Đến: " + formatDisplayDate(end));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDatePicker(boolean isStartDate) {
        try {
            String currentFilter = isStartDate ? startDateFilter : endDateFilter;
            Calendar cal = Calendar.getInstance();
            if (currentFilter != null) {
                Date date = parseDbDate(currentFilter);
                if (date != null) cal.setTime(date);
            }
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        Calendar selectedCal = Calendar.getInstance();
                        selectedCal.set(selectedYear, selectedMonth, selectedDay);
                        String formattedDate = formatDbDate(selectedCal.getTime());

                        if (isStartDate) {
                            startDateFilter = formattedDate;
                        } else {
                            endDateFilter = formattedDate;
                        }
                        updateFilterButtonsLabel();
                        loadRevenueChart();
                    }, year, month, day);
            datePickerDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadRevenueChart() {
        if (layoutChartBars == null) return;

        new Thread(() -> {
            try {
                Date startTemp = parseDbDate(startDateFilter);
                Date endTemp = parseDbDate(endDateFilter);
                if (startTemp != null && endTemp != null && startTemp.after(endTemp)) {
                    String swapTmp = startDateFilter;
                    startDateFilter = endDateFilter;
                    endDateFilter = swapTmp;
                    final String finalStart = startDateFilter;
                    final String finalEnd = endDateFilter;
                    runOnUiThread(() -> {
                        startDateFilter = finalStart;
                        endDateFilter = finalEnd;
                        updateFilterButtonsLabel();
                    });
                }

                List<com.example.shoeapp.data.model.DateRevenue> dbData =
                        db.orderDao().getRevenueBetweenDates(startDateFilter, endDateFilter);

                double totalFilteredRevenue = 0.0;
                Map<String, Double> revenueMap = new HashMap<>();
                for (com.example.shoeapp.data.model.DateRevenue dr : dbData) {
                    revenueMap.put(dr.date, dr.revenue);
                    totalFilteredRevenue += dr.revenue;
                }
                final double finalTotalFilteredRevenue = totalFilteredRevenue;

                List<com.example.shoeapp.data.model.DateRevenue> fullChartData = new ArrayList<>();
                Calendar startCal = Calendar.getInstance();
                Calendar endCal = Calendar.getInstance();

                startCal.setTime(parseDbDate(startDateFilter));
                endCal.setTime(parseDbDate(endDateFilter));

                while (!startCal.after(endCal)) {
                    String dateKey = formatDbDate(startCal.getTime());
                    double revenue = revenueMap.containsKey(dateKey) ? revenueMap.get(dateKey) : 0.0;

                    com.example.shoeapp.data.model.DateRevenue item = new com.example.shoeapp.data.model.DateRevenue();
                    item.date = dateKey;
                    item.revenue = revenue;
                    fullChartData.add(item);

                    startCal.add(Calendar.DATE, 1);
                }

                double maxRevenue = 0.0;
                for (com.example.shoeapp.data.model.DateRevenue item : fullChartData) {
                    if (item.revenue > maxRevenue) {
                        maxRevenue = item.revenue;
                    }
                }

                final double finalMaxRev = maxRevenue;

                runOnUiThread(() -> {
                    TextView tvTotalFilteredRevenue = findViewById(R.id.tv_total_filtered_revenue);
                    if (tvTotalFilteredRevenue != null) {
                        tvTotalFilteredRevenue.setText(currencyFormat.format(finalTotalFilteredRevenue));
                    }

                    layoutChartBars.removeAllViews();

                    if (fullChartData.isEmpty()) {
                        if (tvChartNoData != null) tvChartNoData.setVisibility(View.VISIBLE);
                        return;
                    } else {
                        if (tvChartNoData != null) tvChartNoData.setVisibility(View.GONE);
                    }

                    float scale = getResources().getDisplayMetrics().density;
                    int barWidthPx = (int) (26 * scale);
                    int maxBarHeightPx = (int) (115 * scale);
                    int minBarHeightPx = (int) (4 * scale);

                    for (com.example.shoeapp.data.model.DateRevenue item : fullChartData) {
                        View barView = LayoutInflater.from(this).inflate(R.layout.item_admin_chart_bar, layoutChartBars, false);

                        TextView tvAmount = barView.findViewById(R.id.tv_bar_amount);
                        View barColumn = barView.findViewById(R.id.view_bar_column);
                        TextView tvDate = barView.findViewById(R.id.tv_bar_date);

                        if (tvAmount != null) {
                            if (item.revenue > 0) {
                                tvAmount.setText(formatShortCurrency(item.revenue));
                                tvAmount.setVisibility(View.VISIBLE);
                            } else {
                                tvAmount.setText("0");
                                tvAmount.setVisibility(View.INVISIBLE);
                            }
                        }

                        int barHeight = minBarHeightPx;
                        if (finalMaxRev > 0.0 && item.revenue > 0.0) {
                            barHeight = (int) ((item.revenue / finalMaxRev) * maxBarHeightPx);
                            if (barHeight < minBarHeightPx) barHeight = minBarHeightPx;
                        }

                        if (barColumn != null) {
                            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) barColumn.getLayoutParams();
                            params.width = barWidthPx;
                            params.height = barHeight;
                            barColumn.setLayoutParams(params);
                            barColumn.setBackgroundResource(R.drawable.bg_bar_column);

                            barColumn.setOnClickListener(v -> {
                                try {
                                    Date d = parseDbDate(item.date);
                                    String dateFormatted = d != null ? formatDisplayDate(d) : item.date;
                                    String msg = "Ngày " + dateFormatted + "\nDoanh thu: " + currencyFormat.format(item.revenue);
                                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }

                        if (tvDate != null) {
                            try {
                                Date d = parseDbDate(item.date);
                                if (d != null) {
                                    tvDate.setText(new SimpleDateFormat("dd/MM", Locale.US).format(d));
                                } else {
                                    tvDate.setText(item.date);
                                }
                            } catch (Exception e) {
                                tvDate.setText(item.date);
                            }
                        }

                        layoutChartBars.addView(barView);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String formatShortCurrency(double value) {
        if (value >= 1000000.0) {
            return String.format(Locale.US, "%.1fM", value / 1000000.0).replace(".0", "");
        } else if (value >= 1000.0) {
            return String.format(Locale.US, "%.0fk", value / 1000.0);
        } else {
            return String.format(Locale.US, "%.0f", value);
        }
    }
}
