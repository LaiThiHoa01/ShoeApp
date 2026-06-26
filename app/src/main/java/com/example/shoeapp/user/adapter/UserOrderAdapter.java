package com.example.shoeapp.user.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.R;
import com.example.shoeapp.data.entity.Order;
import com.example.shoeapp.data.model.OrderView;
import com.example.shoeapp.user.ImageLoader;
import com.example.shoeapp.user.OrderDetailActivity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserOrderAdapter extends RecyclerView.Adapter<UserOrderAdapter.OrderViewHolder> {
    private final Context context;
    private final List<OrderView> orders = new ArrayList<>();

    public UserOrderAdapter(Context context) {
        this.context = context;
    }

    public void submitList(List<OrderView> list) {
        this.orders.clear();
        this.orders.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderView orderView = orders.get(position);
        holder.bind(orderView);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private final TextView badgePending;
        private final TextView badgeProcessing;
        private final TextView badgeShipping;
        private final TextView badgeDelivered;
        private final TextView badgeCancelled;
        private final TextView dateText;
        private final TextView nameText;
        private final android.widget.ImageView productImage;
        private final TextView itemCountText;
        private final TextView totalText;
        private final View btnDetails;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            badgePending = itemView.findViewById(R.id.badge_pending);
            badgeProcessing = itemView.findViewById(R.id.badge_processing);
            badgeShipping = itemView.findViewById(R.id.badge_shipping);
            badgeDelivered = itemView.findViewById(R.id.badge_delivered);
            badgeCancelled = itemView.findViewById(R.id.badge_cancelled);
            dateText = itemView.findViewById(R.id.order_date);
            nameText = itemView.findViewById(R.id.order_name);
            productImage = itemView.findViewById(R.id.order_product_image);
            itemCountText = itemView.findViewById(R.id.order_item_count);
            totalText = itemView.findViewById(R.id.order_total_price);
            btnDetails = itemView.findViewById(R.id.btn_order_details);
        }

        public void bind(OrderView orderView) {
            Order order = orderView.order;
            
            // Set Status Badge Visibility
            String status = order.orderStatus != null ? order.orderStatus : "PENDING";
            badgePending.setVisibility(("PENDING".equals(status) || "NEW".equals(status)) ? View.VISIBLE : View.GONE);
            badgeProcessing.setVisibility("PROCESSING".equals(status) ? View.VISIBLE : View.GONE);
            badgeShipping.setVisibility(("SHIPPING".equals(status) || "SHIPPED".equals(status)) ? View.VISIBLE : View.GONE);
            badgeDelivered.setVisibility(("DELIVERED".equals(status) || "COMPLETED".equals(status)) ? View.VISIBLE : View.GONE);
            badgeCancelled.setVisibility("CANCELLED".equals(status) ? View.VISIBLE : View.GONE);

            // Set Date
            dateText.setText(order.createdAt != null ? order.createdAt : "");

            // Set Product Image and Name
            String displayName = orderView.firstProductName != null && !orderView.firstProductName.trim().isEmpty()
                    ? orderView.firstProductName
                    : "Đơn hàng #" + order.id;
            nameText.setText(displayName);

            ImageLoader.load(orderView.firstProductImage, productImage, R.drawable.ic_shoe);

            // Set Item Count directly from data model
            itemCountText.setText(orderView.totalItems + " sản phẩm");

            // Set Total
            NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            double totalVal = order.grandTotal != null ? order.grandTotal : 0.0;
            totalText.setText(formatter.format(Math.round(totalVal)) + " đ");

            // Details Button Click
            btnDetails.setOnClickListener(v -> {
                Intent intent = new Intent(context, OrderDetailActivity.class);
                intent.putExtra("order_id", order.id);
                context.startActivity(intent);
            });
        }
    }
}
