package com.example.shoeapp.user;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.R;
import com.example.shoeapp.authentication.SessionManager;
import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.model.OrderView;
import com.example.shoeapp.ui.BaseSoleStepActivity;
import com.example.shoeapp.ui.BottomNavHelper;
import com.example.shoeapp.user.adapter.UserOrderAdapter;

import java.util.ArrayList;
import java.util.List;

public class MyOrdersActivity extends BaseSoleStepActivity {
    private RecyclerView recyclerView;
    private UserOrderAdapter adapter;
    private TextView badgeText;
    private TextView tabAll, tabPending, tabShipping, tabDelivered;
    private AppDatabase db;
    private final List<OrderView> allOrders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);
        setupScreen(BottomNavHelper.TAG_ORDERS);

        db = AppDatabase.getDatabase(this);
        badgeText = findViewById(R.id.orders_count_badge);
        recyclerView = findViewById(R.id.orders_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserOrderAdapter(this);
        recyclerView.setAdapter(adapter);

        // Bind tabs
        tabAll = findViewById(R.id.tab_all);
        tabPending = findViewById(R.id.tab_pending);
        tabShipping = findViewById(R.id.tab_shipping);
        tabDelivered = findViewById(R.id.tab_delivered);

        tabAll.setOnClickListener(v -> {
            selectTab(tabAll, tabPending, tabShipping, tabDelivered);
            filterOrders("ALL");
        });
        tabPending.setOnClickListener(v -> {
            selectTab(tabPending, tabAll, tabShipping, tabDelivered);
            filterOrders("PENDING");
        });
        tabShipping.setOnClickListener(v -> {
            selectTab(tabShipping, tabAll, tabPending, tabDelivered);
            filterOrders("SHIPPING");
        });
        tabDelivered.setOnClickListener(v -> {
            selectTab(tabDelivered, tabAll, tabPending, tabShipping);
            filterOrders("DELIVERED");
        });
        tabAll.setSelected(true);
        loadOrdersFromDb();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrdersFromDb();
    }

    private void loadOrdersFromDb() {
        int loggedInUserId = SessionManager.getUserId(this);

        if (loggedInUserId == -1) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        new Thread(() -> {
            List<OrderView> list = db.orderDao().getOrdersByUserWithCount(loggedInUserId);

            runOnUiThread(() -> {
                allOrders.clear();

                if (list != null) {
                    allOrders.addAll(list);
                }

                badgeText.setText(String.valueOf(allOrders.size()));

                if (tabAll.isSelected()) {
                    filterOrders("ALL");
                } else if (tabPending.isSelected()) {
                    filterOrders("PENDING");
                } else if (tabShipping.isSelected()) {
                    filterOrders("SHIPPING");
                } else if (tabDelivered.isSelected()) {
                    filterOrders("DELIVERED");
                }
            });
        }).start();
    }

    private void filterOrders(String type) {
        List<OrderView> filteredList = new ArrayList<>();
        for (OrderView orderView : allOrders) {
            String status = orderView.order.orderStatus != null ? orderView.order.orderStatus : "PENDING";
            switch (type) {
                case "ALL":
                    filteredList.add(orderView);
                    break;
                case "PENDING":
                    if ("PENDING".equals(status) || "NEW".equals(status) || "PROCESSING".equals(status)) {
                        filteredList.add(orderView);
                    }
                    break;
                case "SHIPPING":
                    if ("SHIPPED".equals(status) || "SHIPPING".equals(status)) {
                        filteredList.add(orderView);
                    }
                    break;
                case "DELIVERED":
                    if ("DELIVERED".equals(status) || "COMPLETED".equals(status)) {
                        filteredList.add(orderView);
                    }
                    break;
            }
        }
        adapter.submitList(filteredList);
    }

    private void selectTab(TextView selected, TextView t1, TextView t2, TextView t3) {
        selected.setSelected(true);
        t1.setSelected(false);
        t2.setSelected(false);
        t3.setSelected(false);
    }
}
