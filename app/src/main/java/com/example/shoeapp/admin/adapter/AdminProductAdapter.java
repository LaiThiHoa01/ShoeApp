package com.example.shoeapp.admin.adapter;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.model.Product;
import com.example.shoeapp.R;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import com.example.shoeapp.user.ImageLoader;
import com.example.shoeapp.user.ProductDetailActivity;
import com.example.shoeapp.Helper.Helpers;

public class AdminProductAdapter
        extends ListAdapter<Product, AdminProductAdapter.ViewHolder> {

    public interface OnProductActionListener {
        void onEditClick(Product product, int position);
        void onDeleteClick(Product product, int position);
        void onVariantsClick(Product product, int position);
    }

    private static final DiffUtil.ItemCallback<Product> DIFF_CALLBACK = new DiffUtil.ItemCallback<Product>() {
        @Override
        public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.getId() == newItem.getId()
                    && Objects.equals(oldItem.getName(), newItem.getName())
                    && Objects.equals(oldItem.getBrand(), newItem.getBrand())
                    && Objects.equals(oldItem.getCategory(), newItem.getCategory())
                    && Double.compare(oldItem.getPrice(), newItem.getPrice()) == 0
                    && Double.compare(oldItem.getOriginalPrice(), newItem.getOriginalPrice()) == 0
                    && oldItem.getStock() == newItem.getStock()
                    && oldItem.isNew() == newItem.isNew()
                    && Objects.equals(oldItem.getSizes(), newItem.getSizes())
                    && Float.compare(oldItem.getRating(), newItem.getRating()) == 0
                    && oldItem.getReviewCount() == newItem.getReviewCount()
                    && oldItem.getImageResId() == newItem.getImageResId()
                    && Objects.equals(oldItem.getImageUrl(), newItem.getImageUrl())
                    && oldItem.isAvailable() == newItem.isAvailable()
                    && oldItem.isDiscontinued() == newItem.isDiscontinued();
        }
    };

    private final Context                context;
    private final OnProductActionListener listener;

    public AdminProductAdapter(Context context,
                               OnProductActionListener listener) {
        super(DIFF_CALLBACK);
        this.context  = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_admin_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = getItem(position);
        holder.bind(product, position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView   image;
        final TextView    brand;
        final TextView    name;
        final TextView    badgeNew;
        final TextView    price;
        final TextView    priceOriginal;
        final TextView    stock;
        final TextView    size1, size2, size3, size4, sizeMore;
        final TextView    rating;
        final ImageButton btnEdit;
        final ImageButton btnDelete;
        final ImageButton btnVariants;

        final View        color1;
        final View        color2;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image         = itemView.findViewById(R.id.admin_prod_image);
            brand         = itemView.findViewById(R.id.admin_prod_brand);
            name          = itemView.findViewById(R.id.admin_prod_name);
            badgeNew      = itemView.findViewById(R.id.admin_prod_badge_new);
            price         = itemView.findViewById(R.id.admin_prod_price);
            priceOriginal = itemView.findViewById(R.id.admin_prod_price_original);
            stock         = itemView.findViewById(R.id.admin_prod_stock);
            size1         = itemView.findViewById(R.id.admin_prod_size_1);
            size2         = itemView.findViewById(R.id.admin_prod_size_2);
            size3         = itemView.findViewById(R.id.admin_prod_size_3);
            size4         = itemView.findViewById(R.id.admin_prod_size_4);
            sizeMore      = itemView.findViewById(R.id.admin_prod_size_more);
            rating        = itemView.findViewById(R.id.admin_prod_rating);
            btnEdit       = itemView.findViewById(R.id.admin_prod_btn_edit);
            btnDelete     = itemView.findViewById(R.id.admin_prod_btn_delete);
            btnVariants   = itemView.findViewById(R.id.admin_prod_btn_variants);
            color1        = itemView.findViewById(R.id.admin_prod_color_1);
            color2        = itemView.findViewById(R.id.admin_prod_color_2);
        }

        void bind(Product product, int position) {
            ImageLoader.load(product.getImageUrl(), image, product.getImageResId());
            brand.setText(product.getBrand());
            name.setText(product.getName());
            badgeNew.setVisibility(product.isNew() ? View.VISIBLE : View.GONE);
            price.setText(Helpers.formatPrice(product.getPrice()));
            if (product.getOriginalPrice() > product.getPrice()) {
                priceOriginal.setText(Helpers.formatPrice(product.getOriginalPrice()));
                priceOriginal.setPaintFlags(priceOriginal.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                priceOriginal.setVisibility(View.VISIBLE);
            } else {
                priceOriginal.setVisibility(View.GONE);
            }
            if (product.isDiscontinued() || !product.isAvailable()) {
                itemView.setAlpha(0.5f);
                stock.setText("ĐÃ ẨN");
                stock.setBackgroundResource(R.drawable.bg_admin_stock_low);
                stock.setTextColor(ContextCompat.getColor(context, R.color.status_error_light));
                btnDelete.setColorFilter(ContextCompat.getColor(context, R.color.status_success_light));
            } else {
                itemView.setAlpha(1.0f);
                int stockQty = product.getStock();
                stock.setText(context.getString(R.string.admin_stock_format, stockQty));
                if (stockQty < 15) {
                    stock.setBackgroundResource(R.drawable.bg_admin_stock_low);
                    stock.setTextColor(ContextCompat.getColor(context, R.color.status_error_light));
                } else {
                    stock.setBackgroundResource(R.drawable.bg_admin_stock_ok);
                    stock.setTextColor(ContextCompat.getColor(context, R.color.status_success_light));
                }
                btnDelete.setColorFilter(ContextCompat.getColor(context, R.color.status_error_light));
            }
            List<Integer> sizes = product.getSizes();
            TextView[] sizeViews = { size1, size2, size3, size4 };
            int displayCount = Math.min(sizes.size(), 4);

            for (int i = 0; i < 4; i++) {
                if (i < displayCount) {
                    sizeViews[i].setText(String.valueOf(sizes.get(i)));
                    sizeViews[i].setVisibility(View.VISIBLE);
                } else {
                    sizeViews[i].setVisibility(View.GONE);
                }
            }
            int extra = sizes.size() - 4;
            if (extra > 0) {
                sizeMore.setText(String.format(Locale.US, "+%d", extra));
                sizeMore.setVisibility(View.VISIBLE);
            } else {
                sizeMore.setVisibility(View.GONE);
            }
            List<String> colors = product.getColors();
            if (colors != null && !colors.isEmpty()) {
                try {
                    String hex = colors.get(0);
                    android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
                    gd.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                    gd.setColor(android.graphics.Color.parseColor(hex));
                    color1.setBackground(gd);
                    color1.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    color1.setVisibility(View.GONE);
                }

                if (colors.size() > 1) {
                    try {
                        String hex = colors.get(1);
                        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
                        gd.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                        gd.setColor(android.graphics.Color.parseColor(hex));
                        color2.setBackground(gd);
                        color2.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        color2.setVisibility(View.GONE);
                    }
                } else {
                    color2.setVisibility(View.GONE);
                }
            } else {
                color1.setVisibility(View.GONE);
                color2.setVisibility(View.GONE);
            }
            rating.setText(String.format(Locale.US, "%.1f (%d)",
                    product.getRating(), product.getReviewCount()));
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                context.startActivity(intent);
            });
            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(product, position);
            });
            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(product, position);
            });
            btnVariants.setOnClickListener(v -> {
                if (listener != null) listener.onVariantsClick(product, position);
            });
        }
    }
}