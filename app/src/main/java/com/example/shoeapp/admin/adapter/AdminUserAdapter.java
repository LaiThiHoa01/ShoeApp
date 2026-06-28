package com.example.shoeapp.admin.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.R;
import com.example.shoeapp.data.entity.User;
import com.example.shoeapp.data.model.UserWithStats;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Objects;

public class AdminUserAdapter extends ListAdapter<UserWithStats, AdminUserAdapter.ViewHolder> {

    public interface OnUserActionListener {
        void onEditClick(UserWithStats userWithStats);
    }

    private static final DiffUtil.ItemCallback<UserWithStats> DIFF_CALLBACK = new DiffUtil.ItemCallback<UserWithStats>() {
        @Override
        public boolean areItemsTheSame(@NonNull UserWithStats oldItem, @NonNull UserWithStats newItem) {
            return oldItem.user.id == newItem.user.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull UserWithStats oldItem, @NonNull UserWithStats newItem) {
            return oldItem.user.id == newItem.user.id
                    && Objects.equals(oldItem.user.fullName, newItem.user.fullName)
                    && Objects.equals(oldItem.user.email, newItem.user.email)
                    && Objects.equals(oldItem.user.phoneNumber, newItem.user.phoneNumber)
                    && Objects.equals(oldItem.user.role, newItem.user.role)
                    && oldItem.user.isActive == newItem.user.isActive
                    && oldItem.orderCount == newItem.orderCount
                    && Double.compare(oldItem.spentAmount, newItem.spentAmount) == 0;
        }
    };

    private final Context context;
    private final OnUserActionListener listener;
    private final DecimalFormat currencyFormat = new DecimalFormat("#,### ₫");

    public AdminUserAdapter(Context context, OnUserActionListener listener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvInitials;
        final TextView tvName;
        final TextView tvRoleBadge;
        final TextView tvStatusBadge;
        final TextView tvEmail;
        final TextView tvOrdersCount;
        final TextView tvSpentAmount;
        final TextView tvJoinedDate;
        final View btnEdit;
        final View avatarContainer;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitials = itemView.findViewById(R.id.tv_initials);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvRoleBadge = itemView.findViewById(R.id.tv_role_badge);
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
            tvEmail = itemView.findViewById(R.id.tv_user_email);
            tvOrdersCount = itemView.findViewById(R.id.tv_orders_count);
            tvSpentAmount = itemView.findViewById(R.id.tv_spent_amount);
            tvJoinedDate = itemView.findViewById(R.id.tv_joined_date);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            avatarContainer = itemView.findViewById(R.id.avatar_container);
        }

        void bind(UserWithStats item) {
            User u = item.user;

            // Thiết lập Initials avatar
            if (u.fullName != null && !u.fullName.trim().isEmpty()) {
                String[] words = u.fullName.trim().split("\\s+");
                String initials = "";
                if (words.length > 0) {
                    initials += words[0].substring(0, 1).toUpperCase();
                    if (words.length > 1) {
                        initials += words[words.length - 1].substring(0, 1).toUpperCase();
                    }
                }
                tvInitials.setText(initials);
            } else {
                tvInitials.setText("U");
            }

            // Đổi màu nền avatar ngẫu nhiên hoặc theo ID để đẹp mắt
            int[] colors = {Color.parseColor("#EF4444"), Color.parseColor("#F59E0B"), Color.parseColor("#10B981"), Color.parseColor("#3B82F6"), Color.parseColor("#8B5CF6")};
            int colorIndex = Math.abs(u.id) % colors.length;
            avatarContainer.setBackgroundTintList(ColorStateList.valueOf(colors[colorIndex]));

            // Tên và email
            tvName.setText(u.fullName);
            tvEmail.setText(u.email);

            // Vai trò (Role)
            if ("ADMIN".equals(u.role)) {
                tvRoleBadge.setText("Quản trị");
                tvRoleBadge.setTextColor(ContextCompat.getColor(context, R.color.brand_orange));
                tvRoleBadge.setBackgroundResource(R.drawable.bg_status_delivered);
                tvRoleBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1AF97316")));
            } else {
                tvRoleBadge.setText("Khách hàng");
                tvRoleBadge.setTextColor(ContextCompat.getColor(context, R.color.stat_orders));
                tvRoleBadge.setBackgroundResource(R.drawable.bg_status_delivered);
                tvRoleBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1A3B82F6")));
            }

            // Trạng thái hoạt động
            if (u.isActive) {
                tvStatusBadge.setText("Hoạt động");
                tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_success));
                tvStatusBadge.setBackgroundResource(R.drawable.bg_status_delivered);
                tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1A10B981")));
            } else {
                tvStatusBadge.setText("Ngừng hoạt động");
                tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_error));
                tvStatusBadge.setBackgroundResource(R.drawable.bg_status_cancelled);
                tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1AEF4444")));
            }

            // Đơn hàng và chi tiêu
            tvOrdersCount.setText(String.format(Locale.getDefault(), "%d đơn hàng", item.orderCount));
            tvSpentAmount.setText(String.format(Locale.getDefault(), "Đã chi: %s", currencyFormat.format(item.spentAmount)));

            // Ngày tham gia
            String joined = u.createdAt != null ? u.createdAt : "";
            if (joined.length() >= 10) {
                joined = joined.substring(0, 10);
            }
            tvJoinedDate.setText(String.format(Locale.getDefault(), "Đã tham gia: %s", joined));

            // Listeners
            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(item);
            });
        }
    }
}
