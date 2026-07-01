package com.example.shoeapp.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.R;
import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.entity.Order;
import com.example.shoeapp.data.entity.User;
import com.example.shoeapp.data.model.OrderItemView;
import com.example.shoeapp.ui.BaseSoleStepActivity;
import com.example.shoeapp.ui.BottomNavHelper;
import com.example.shoeapp.user.adapter.OrderDetailItemAdapter;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderDetailActivity extends BaseSoleStepActivity {
    
    private OrderDetailItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        setupScreen(BottomNavHelper.TAG_ORDERS);

        // Setup RecyclerView
        RecyclerView recyclerView = findViewById(R.id.order_items_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderDetailItemAdapter(this);
        recyclerView.setAdapter(adapter);

        int orderId = getIntent().getIntExtra("order_id", -1);
        if (orderId == -1) {
            Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        int orderId = getIntent().getIntExtra("order_id", -1);
        if (orderId != -1) {
            loadOrderData(orderId);
        }
    }
    
    private void loadOrderData(int orderId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            Order order = db.orderDao().getOrderById(orderId);

            if (order != null) {
                User user = db.userDao().getUserById(order.userId);
                List<OrderItemView> items = db.orderDao().getOrderItems(orderId);

                java.util.List<OrderItemView> unreviewedItems = new java.util.ArrayList<>();
                java.util.Set<Integer> checkedProducts = new java.util.HashSet<>();

                for (OrderItemView item : items) {
                    if (checkedProducts.contains(item.productId)) {
                        continue;
                    }
                    checkedProducts.add(item.productId);
                    
                    boolean isReviewed = false;
                    List<com.example.shoeapp.data.entity.ProductReview> reviews = db.productDao().getReviewsByProduct(item.productId);
                    for (com.example.shoeapp.data.entity.ProductReview r : reviews) {
                        if (r.userId == order.userId && r.orderId != null && r.orderId.equals(order.id)) {
                            isReviewed = true;
                            break;
                        }
                    }
                    if (!isReviewed) {
                        unreviewedItems.add(item);
                    }
                }

                if (!isFinishing() && !isDestroyed()) {
                    runOnUiThread(() -> populateData(order, user, items, unreviewedItems));
                }
            } else {
                if (!isFinishing() && !isDestroyed()) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Đơn hàng không tồn tại", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            }
        });
        executor.shutdown();
    }
    
    private void populateData(Order order, User user, List<OrderItemView> items, List<OrderItemView> unreviewedItems) {
        // Order and Date
        ((TextView) findViewById(R.id.order_detail_id_text)).setText(order.ordersId);
        ((TextView) findViewById(R.id.order_reference_text)).setText(order.ordersId);

        findViewById(R.id.copy_order_reference_button).setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Order Reference", order.ordersId);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Đã sao chép mã đơn hàng", Toast.LENGTH_SHORT).show();
            }
        });
        
        TextView dateText = findViewById(R.id.order_detail_date_text);
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(order.createdAt);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd 'Tháng' MM, yyyy", new Locale("vi", "VN"));
            dateText.setText(outputFormat.format(date));
        } catch (Exception e) {
            dateText.setText(order.createdAt);
        }

        // Status
        TextView statusBadge = findViewById(R.id.order_detail_status_badge);
        if ("PENDING".equalsIgnoreCase(order.orderStatus)) {
            statusBadge.setText(R.string.orders_tab_pending);
            statusBadge.setActivated(false);
            statusBadge.setSelected(false);
        } else if ("DELIVERED".equalsIgnoreCase(order.orderStatus) || "COMPLETED".equalsIgnoreCase(order.orderStatus)) {
            statusBadge.setText(R.string.status_delivered);
            statusBadge.setActivated(true);
            statusBadge.setSelected(false);
        } else if ("CANCELLED".equalsIgnoreCase(order.orderStatus)) {
            statusBadge.setText(R.string.status_cancelled);
            statusBadge.setActivated(false);
            statusBadge.setSelected(true);
        } else {
            statusBadge.setText(order.orderStatus);
            statusBadge.setActivated(false);
            statusBadge.setSelected(false);
        }

        // Address
        if (user != null) {
            ((TextView) findViewById(R.id.shipping_name_text)).setText(user.fullName);
        } else {
            findViewById(R.id.shipping_name_text).setVisibility(View.GONE);
        }
        ((TextView) findViewById(R.id.shipping_line_one_text)).setText(order.shippingAddress);
        findViewById(R.id.shipping_line_two_text).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.shipping_phone_text)).setText(order.phoneNumber);

        // Payment
        String methodText = "Thanh toán khi nhận hàng (COD)";
        if ("ZALOPAY".equalsIgnoreCase(order.paymentMethod)) {
            methodText = "Ví ZaloPay";
        }
        ((TextView) findViewById(R.id.payment_method_text)).setText(methodText);
        
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        ((TextView) findViewById(R.id.payment_subtotal_value)).setText(formatter.format(order.subTotal) + " đ");
        ((TextView) findViewById(R.id.payment_shipping_value)).setText(formatter.format(order.shippingFee) + " đ");
        ((TextView) findViewById(R.id.payment_total_value)).setText(formatter.format(order.grandTotal) + " đ");

        // Items list
        adapter.submitList(items);

        // Rate Product Button
        View rateBtn = findViewById(R.id.rate_products_button);
        boolean isDelivered = "DELIVERED".equalsIgnoreCase(order.orderStatus) || "COMPLETED".equalsIgnoreCase(order.orderStatus);
        
        if (isDelivered && unreviewedItems != null && !unreviewedItems.isEmpty()) {
            rateBtn.setVisibility(View.VISIBLE);
            rateBtn.setOnClickListener(v -> {
                if (unreviewedItems.size() == 1) {
                    Intent intent = new Intent(this, ProductReviewActivity.class);
                    intent.putExtra("product_id", unreviewedItems.get(0).productId);
                    intent.putExtra("user_id", order.userId);
                    intent.putExtra("order_id", order.id);
                    startActivity(intent);
                } else {
                    String[] names = new String[unreviewedItems.size()];
                    for (int i = 0; i < unreviewedItems.size(); i++) {
                        names[i] = unreviewedItems.get(i).productName;
                    }
                    new android.app.AlertDialog.Builder(this)
                            .setTitle("Chọn sản phẩm để đánh giá")
                            .setItems(names, (dialog, which) -> {
                                Intent intent = new Intent(this, ProductReviewActivity.class);
                                intent.putExtra("product_id", unreviewedItems.get(which).productId);
                                intent.putExtra("user_id", order.userId);
                                intent.putExtra("order_id", order.id);
                                startActivity(intent);
                            })
                            .setNegativeButton("Huỷ", null)
                            .show();
                }
            });
        } else {
            rateBtn.setVisibility(View.GONE);
        }
    }
}
