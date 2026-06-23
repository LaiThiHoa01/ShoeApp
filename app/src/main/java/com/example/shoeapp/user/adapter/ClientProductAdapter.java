package com.example.shoeapp.user.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.R;
import com.example.shoeapp.model.Product;

import java.util.List;
import java.util.Locale;

public class ClientProductAdapter extends RecyclerView.Adapter<ClientProductAdapter.ViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    private final Context context;
    private final List<Product> products;
    private final OnProductClickListener listener;

    public ClientProductAdapter(Context context, List<Product> products, OnProductClickListener listener) {
        this.context = context;
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_client_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(products.get(position), position);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final FrameLayout imagePanel;
        final TextView badgeNew;
        final TextView badgeDiscount;
        final ImageView image;
        final TextView brand;
        final TextView name;
        final TextView rating;
        final TextView price;
        final TextView originalPrice;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imagePanel = itemView.findViewById(R.id.client_product_image_panel);
            badgeNew = itemView.findViewById(R.id.client_product_badge_new);
            badgeDiscount = itemView.findViewById(R.id.client_product_badge_discount);
            image = itemView.findViewById(R.id.client_product_image);
            brand = itemView.findViewById(R.id.client_product_brand);
            name = itemView.findViewById(R.id.client_product_name);
            rating = itemView.findViewById(R.id.client_product_rating);
            price = itemView.findViewById(R.id.client_product_price);
            originalPrice = itemView.findViewById(R.id.client_product_original_price);
        }

        void bind(Product product, int position) {
            imagePanel.setBackgroundResource(backgroundFor(position));
            image.setImageResource(product.getImageResId() == 0 ? R.drawable.ic_shoe : product.getImageResId());
            badgeNew.setVisibility(product.isNew() ? View.VISIBLE : View.GONE);

            int discount = discountPercent(product);
            if (discount > 0) {
                badgeDiscount.setVisibility(View.VISIBLE);
                badgeDiscount.setText(String.format(Locale.US, "-%d%%", discount));
            } else {
                badgeDiscount.setVisibility(View.GONE);
            }

            brand.setText(displayBrand(product.getBrand()));
            name.setText(product.getName());
            rating.setText(String.format(Locale.US, "* %.1f  (%d)", product.getRating(), product.getReviewCount()));
            price.setText(String.format(Locale.US, "$%.2f", product.getPrice()));
            originalPrice.setText(String.format(Locale.US, "$%.2f", product.getOriginalPrice()));
            originalPrice.setPaintFlags(originalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
        }

        private int backgroundFor(int position) {
            int[] backgrounds = {
                    R.drawable.bg_home_product_image_light,
                    R.drawable.bg_home_product_image_warm,
                    R.drawable.bg_home_product_image_blue,
                    R.drawable.bg_home_product_image_gray
            };
            return backgrounds[position % backgrounds.length];
        }

        private int discountPercent(Product product) {
            if (product.getOriginalPrice() <= product.getPrice()) {
                return 0;
            }
            double discount = 1 - product.getPrice() / product.getOriginalPrice();
            return (int) Math.round(discount * 100);
        }

        private String displayBrand(String rawBrand) {
            int separator = rawBrand.indexOf(" - ");
            return separator >= 0 ? rawBrand.substring(0, separator) : rawBrand;
        }
    }
}
