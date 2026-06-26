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

        findViewById(R.id.rate_products_button).setOnClickListener(v ->
                startActivity(new Intent(this, ProductReviewActivity.class)));
                
        // Setup RecyclerView
        RecyclerView recyclerView = findViewById(R.id.order_items_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderDetailItemAdapter(this);
        recyclerView.setAdapter(adapter);

        // Fetch Data
        int orderId = getIntent().getIntExtra("order_id", -1);
        if (orderId != -1) {
            loadOrderData(orderId);
        } else {
            Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
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
                
                runOnUiThread(() -> populateData(order, user, items));
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Đơn hàng không tồn tại", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }
    
    private void populateData(Order order, User user, List<OrderItemView> items) {
        // Order Reference and Date
        ((TextView) findViewById(R.id.order_detail_id_text)).setText(order.ordersId);
        
        TextView dateText = findViewById(R.id.order_detail_date_text);
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(order.createdAt);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd 'Tháng' MM, yyyy", new Locale("vi", "VN"));
            dateText.setText(outputFormat.format(date));
        } catch (Exception e) {
            dateText.setText(order.createdAt);
        }

        // Status Badge
        TextView statusBadge = findViewById(R.id.order_detail_status_badge);
        if ("PENDING".equalsIgnoreCase(order.orderStatus)) {
            statusBadge.setText(R.string.orders_tab_pending);
            statusBadge.setBackgroundResource(R.drawable.bg_status_pending);
            statusBadge.setTextColor(getResources().getColor(R.color.brand_orange));
        } else if ("DELIVERED".equalsIgnoreCase(order.orderStatus) || "COMPLETED".equalsIgnoreCase(order.orderStatus)) {
            statusBadge.setText(R.string.status_delivered);
            statusBadge.setBackgroundResource(R.drawable.bg_status_delivered);
            statusBadge.setTextColor(getResources().getColor(R.color.status_success));
        } else if ("CANCELLED".equalsIgnoreCase(order.orderStatus)) {
            statusBadge.setText(R.string.status_cancelled);
            statusBadge.setBackgroundResource(R.drawable.bg_status_cancelled);
            statusBadge.setTextColor(getResources().getColor(R.color.status_error));
        } else {
            statusBadge.setText(order.orderStatus);
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
        ((TextView) findViewById(R.id.payment_method_text)).setText(
                "MOMO".equalsIgnoreCase(order.paymentMethod) ? "Ví MoMo" : "Thanh toán khi nhận hàng (COD)"
        );
        
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        ((TextView) findViewById(R.id.payment_subtotal_value)).setText(formatter.format(order.subTotal) + " đ");
        ((TextView) findViewById(R.id.payment_shipping_value)).setText(formatter.format(order.shippingFee) + " đ");
        ((TextView) findViewById(R.id.payment_total_value)).setText(formatter.format(order.grandTotal) + " đ");

        // Items list
        adapter.submitList(items);
    }
}
