package com.example.shoeapp.user.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shoeapp.R;
import com.example.shoeapp.data.entity.Promotion;

import java.util.List;

public class PromotionBannerAdapter extends RecyclerView.Adapter<PromotionBannerAdapter.BannerViewHolder> {

    private final List<Promotion> promotions;
    private final OnBannerClickListener listener;

    public interface OnBannerClickListener {
        void onBannerClick(Promotion promotion);
    }

    public PromotionBannerAdapter(List<Promotion> promotions, OnBannerClickListener listener) {
        this.promotions = promotions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_promotion_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Promotion promotion = promotions.get(position);
        holder.bind(promotion);
    }

    @Override
    public int getItemCount() {
        return promotions.size();
    }

    class BannerViewHolder extends RecyclerView.ViewHolder {
        private final ImageView bannerImage;
        private final TextView bannerTitle;
        private final TextView bannerSubtitle;
        private final TextView bannerTag;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            bannerImage = itemView.findViewById(R.id.banner_image);
            bannerTitle = itemView.findViewById(R.id.banner_title);
            bannerSubtitle = itemView.findViewById(R.id.banner_subtitle);
            bannerTag = itemView.findViewById(R.id.banner_tag);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onBannerClick(promotions.get(position));
                }
            });
        }

        public void bind(Promotion promotion) {
            bannerTitle.setText(promotion.name);
            bannerSubtitle.setText(promotion.subtitle != null ? promotion.subtitle : "");
            
            if ("PERCENTAGE".equals(promotion.discountType)) {
                bannerTag.setText("GIẢM " + (int) promotion.discountValue + "%");
            } else {
                bannerTag.setText("SALE HOT");
            }

            if (promotion.bannerUrl != null && !promotion.bannerUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(promotion.bannerUrl)
                        .into(bannerImage);
            } else {
                bannerImage.setImageResource(R.drawable.ic_shoe);
            }
        }
    }
}
