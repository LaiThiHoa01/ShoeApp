package com.example.shoeapp.admin.adapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.R;
import com.example.shoeapp.data.entity.Promotion;
import com.example.shoeapp.user.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class AdminPromotionAdapter extends RecyclerView.Adapter<AdminPromotionAdapter.ViewHolder> {

    private List<Promotion> promotionList = new ArrayList<>();
    private final OnPromotionClickListener listener;
    private final SimpleDateFormat dateFormatDb = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat dateFormatDisplay = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

    public interface OnPromotionClickListener {
        void onPromotionClick(Promotion promotion);
    }

    public AdminPromotionAdapter(OnPromotionClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Promotion> list) {
        this.promotionList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_promotion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Promotion promotion = promotionList.get(position);
        
        holder.tvName.setText(promotion.name);
        holder.tvVoucher.setText(String.format("Mã: %s (Còn: %d)", promotion.voucherCode != null ? promotion.voucherCode : "N/A", promotion.quantity));
        
        if ("PERCENTAGE".equals(promotion.discountType)) {
            holder.tvDiscount.setText(String.format(Locale.US, "Giảm: %.0f%%", promotion.discountValue));
        } else {
            holder.tvDiscount.setText(String.format(Locale.US, "Giảm: %,.0f đ", promotion.discountValue));
        }

        String startDate = formatDisplayDate(promotion.startDate);
        String endDate = formatDisplayDate(promotion.endDate);
        holder.tvDate.setText(String.format("Từ %s - %s", startDate, endDate));

        // Determine status
        boolean isActive = promotion.isActive && promotion.quantity > 0 && isDateInRange(promotion.startDate, promotion.endDate);
        if (isActive) {
            holder.tvStatus.setText("Đang diễn ra");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_delivered);
            holder.tvStatus.setTextColor(android.graphics.Color.BLACK);
        } else {
            holder.tvStatus.setText("Đã kết thúc");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
            holder.tvStatus.setTextColor(android.graphics.Color.WHITE);
        }

        ImageLoader.load(promotion.bannerUrl, holder.ivImage, R.drawable.ic_shoe);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPromotionClick(promotion);
        });
    }
    
    private String formatDisplayDate(String dbDate) {
        try {
            if (dbDate != null && !dbDate.isEmpty()) {
                Date date = dateFormatDb.parse(dbDate);
                if (date != null) {
                    return dateFormatDisplay.format(date);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dbDate;
    }
    
    private boolean isDateInRange(String startStr, String endStr) {
        try {
            Date now = new Date();
            // Start of day
            now.setHours(0); now.setMinutes(0); now.setSeconds(0);
            
            if (startStr != null && !startStr.isEmpty() && endStr != null && !endStr.isEmpty()) {
                Date start = dateFormatDb.parse(startStr);
                Date end = dateFormatDb.parse(endStr);
                
                // End date inclusive by setting it to end of day
                if (end != null) {
                    end.setHours(23); end.setMinutes(59); end.setSeconds(59);
                }
                
                if (start != null && end != null) {
                    return !now.before(start) && !now.after(end);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return promotionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatus, tvName, tvVoucher, tvDiscount, tvDate;
        ImageView ivImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatus = itemView.findViewById(R.id.tv_promo_status);
            tvName = itemView.findViewById(R.id.tv_promo_name);
            tvVoucher = itemView.findViewById(R.id.tv_promo_voucher);
            tvDiscount = itemView.findViewById(R.id.tv_promo_discount);
            tvDate = itemView.findViewById(R.id.tv_promo_date);
            ivImage = itemView.findViewById(R.id.iv_promo_image);
        }
    }
}
