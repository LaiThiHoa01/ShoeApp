package com.example.shoeapp.admin.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.model.Order;
import com.example.shoeapp.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;
public class AdminOrderAdapter
        extends ListAdapter<Order, AdminOrderAdapter.ViewHolder> {

    public interface OnOrderActionListener {
        void onViewDetailsClick(Order order, int position);
        void onMarkShippedClick(Order order, int position);
        void onMarkDeliveredClick(Order order, int position);
        void onCancelOrderClick(Order order, int position);
    }
    private final Context               context;
    private final OnOrderActionListener listener;

    private static final DiffUtil.ItemCallback<Order> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Order>() {
                @Override
                public boolean areItemsTheSame(@NonNull Order oldItem, @NonNull Order newItem) {
                    return oldItem.getOrderId().equals(newItem.getOrderId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Order oldItem, @NonNull Order newItem) {
                    return oldItem.getStatus().equals(newItem.getStatus())
                            && oldItem.getCustomerName().equals(newItem.getCustomerName())
                            && Double.compare(oldItem.getTotal(), newItem.getTotal()) == 0
                            && oldItem.getItemCount() == newItem.getItemCount()
                            && oldItem.getDate().equals(newItem.getDate());
                }
            };

    public AdminOrderAdapter(Context context,
                             OnOrderActionListener listener) {
        super(DIFF_CALLBACK);
        this.context  = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_admin_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = getItem(position);
        holder.bind(order, position);
    }

    // ── ViewHolder ──────────────────────────────────────────────────────────
    class ViewHolder extends RecyclerView.ViewHolder {

        final LinearLayout  statusBadge;
        final TextView      statusText;
        final View          dot1, dot2, dot3;
        final TextView      date;
        final TextView      orderId;
        final TextView      customerName;
        final TextView      total;
        final TextView      itemCount;
        final MaterialButton btnView;
        final MaterialButton btnCancel;
        final MaterialButton btnAction;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            statusBadge  = itemView.findViewById(R.id.admin_order_status_badge);
            statusText   = itemView.findViewById(R.id.admin_order_status_text);
            dot1         = itemView.findViewById(R.id.admin_order_dot_1);
            dot2         = itemView.findViewById(R.id.admin_order_dot_2);
            dot3         = itemView.findViewById(R.id.admin_order_dot_3);
            date         = itemView.findViewById(R.id.admin_order_date);
            orderId      = itemView.findViewById(R.id.admin_order_id);
            customerName = itemView.findViewById(R.id.admin_order_customer);
            total        = itemView.findViewById(R.id.admin_order_total);
            itemCount    = itemView.findViewById(R.id.admin_order_items_count);
            btnView      = itemView.findViewById(R.id.admin_order_btn_view);
            btnCancel    = itemView.findViewById(R.id.admin_order_btn_cancel);
            btnAction    = itemView.findViewById(R.id.admin_order_btn_action);
        }

        void bind(Order order, int position) {
            // ── Thông tin đơn hàng ─────────────────────────────────────────
            orderId.setText(order.getOrderId());
            customerName.setText(order.getCustomerName());
            date.setText(order.getDate());
            total.setText(com.example.shoeapp.Helper.Helpers.formatPrice(order.getTotal()));

            int count = order.getItemCount();
            itemCount.setText(context.getResources()
                    .getQuantityString(R.plurals.admin_items_count, count, count));

            // ── Status badge + progress dots ───────────────────────────────
            applyStatusStyle(order.getStatus());

            // ── Nút View Details ───────────────────────────────────────────
            btnView.setOnClickListener(v -> {
                if (listener != null) listener.onViewDetailsClick(order, position);
            });

            // ── Nút action (Mark as Shipped / Mark as Delivered / disabled) ──
            btnCancel.setVisibility(View.GONE);
            switch (order.getStatus()) {
                case PROCESSING:
                    btnAction.setVisibility(View.VISIBLE);
                    btnAction.setText(context.getString(R.string.admin_mark_shipped));
                    btnAction.setIcon(null);
                    btnAction.setEnabled(true);
                    btnAction.setBackgroundTintList(
                            ContextCompat.getColorStateList(context, R.color.brand_orange));
                    btnAction.setTextColor(
                            ContextCompat.getColor(context, R.color.brand_white));
                    btnAction.setOnClickListener(v -> {
                        if (listener != null) listener.onMarkShippedClick(order, position);
                    });

                    btnCancel.setVisibility(View.VISIBLE);
                    btnCancel.setOnClickListener(v -> {
                        if (listener != null) listener.onCancelOrderClick(order, position);
                    });
                    break;

                case SHIPPED:
                    btnAction.setVisibility(View.VISIBLE);
                    btnAction.setText(context.getString(R.string.admin_mark_delivered));
                    btnAction.setIcon(null);
                    btnAction.setEnabled(true);
                    btnAction.setBackgroundTintList(
                            ContextCompat.getColorStateList(context, R.color.status_info));
                    btnAction.setTextColor(
                            ContextCompat.getColor(context, R.color.brand_white));
                    btnAction.setOnClickListener(v -> {
                        if (listener != null) listener.onMarkDeliveredClick(order, position);
                    });
                    break;

                case DELIVERED:
                    btnAction.setVisibility(View.VISIBLE);
                    btnAction.setText(context.getString(R.string.admin_order_completed));
                    btnAction.setIcon(null);
                    btnAction.setEnabled(false);
                    btnAction.setBackgroundTintList(
                            ContextCompat.getColorStateList(context, R.color.status_success_bg));
                    btnAction.setTextColor(
                            ContextCompat.getColor(context, R.color.status_success));
                    btnAction.setOnClickListener(null);
                    break;

                case CANCELLED:
                    btnAction.setVisibility(View.VISIBLE);
                    btnAction.setText(context.getString(R.string.status_cancelled));
                    btnAction.setIcon(null);
                    btnAction.setEnabled(false);
                    btnAction.setBackgroundTintList(
                            ContextCompat.getColorStateList(context, R.color.status_error_bg));
                    btnAction.setTextColor(
                            ContextCompat.getColor(context, R.color.status_error));
                    btnAction.setOnClickListener(null);
                    break;
            }
        }

        /** Áp màu badge + progress dots theo trạng thái đơn hàng */
        private void applyStatusStyle(Order.Status status) {
            switch (status) {
                case PROCESSING:
                    statusBadge.setBackgroundResource(R.drawable.bg_admin_status_processing);
                    statusText.setText(context.getString(R.string.status_processing));
                    statusText.setTextColor(
                            ContextCompat.getColor(context, R.color.status_warning));
                    setDotColor(dot1, R.color.status_warning);
                    setDotColor(dot2, R.color.border_medium);
                    setDotColor(dot3, R.color.border_medium);
                    break;

                case SHIPPED:
                    statusBadge.setBackgroundResource(R.drawable.bg_admin_status_shipped);
                    statusText.setText(context.getString(R.string.status_shipped));
                    statusText.setTextColor(
                            ContextCompat.getColor(context, R.color.status_info));
                    setDotColor(dot1, R.color.status_info);
                    setDotColor(dot2, R.color.status_info);
                    setDotColor(dot3, R.color.border_medium);
                    break;

                case DELIVERED:
                    statusBadge.setBackgroundResource(R.drawable.bg_admin_status_delivered);
                    statusText.setText(context.getString(R.string.status_delivered));
                    statusText.setTextColor(
                            ContextCompat.getColor(context, R.color.status_success));
                    setDotColor(dot1, R.color.status_success);
                    setDotColor(dot2, R.color.status_info);
                    setDotColor(dot3, R.color.status_success);
                    break;

                case CANCELLED:
                    statusBadge.setBackgroundResource(R.drawable.bg_admin_status_processing);
                    statusText.setText(context.getString(R.string.status_cancelled));
                    statusText.setTextColor(
                            ContextCompat.getColor(context, R.color.status_error));
                    setDotColor(dot1, R.color.status_error);
                    setDotColor(dot2, R.color.border_medium);
                    setDotColor(dot3, R.color.border_medium);
                    break;
            }
        }

        private void setDotColor(View dot, int colorRes) {
            dot.setBackgroundColor(ContextCompat.getColor(context, colorRes));
        }
    }
}
