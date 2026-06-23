package com.example.shoeapp.admin.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.model.Product;
import com.example.shoeapp.R;

import java.util.List;
import java.util.Locale;
public class AdminProductAdapter
        extends RecyclerView.Adapter<AdminProductAdapter.ViewHolder> {

    public interface OnProductActionListener {
        void onEditClick(Product product, int position);
        void onDeleteClick(Product product, int position);
    }

    private final Context                context;
    private final List<Product>          products;
    private final OnProductActionListener listener;

    public AdminProductAdapter(Context context,
                               List<Product> products,
                               OnProductActionListener listener) {
        this.context  = context;
        this.products = products;
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
        Product product = products.get(position);
        holder.bind(product, position);
    }

    @Override
    public int getItemCount() {
        return products.size();
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
        }

        void bind(Product product, int position) {
            // ── Ảnh sản phẩm ───────────────────────────────────────────────
            com.example.shoeapp.user.ImageLoader.load(product.getImageUrl(), image, product.getImageResId());

            // ── Thông tin cơ bản ───────────────────────────────────────────
            brand.setText(product.getBrand());
            name.setText(product.getName());

            // ── Badge NEW ──────────────────────────────────────────────────
            badgeNew.setVisibility(product.isNew() ? View.VISIBLE : View.GONE);

            // ── Giá ────────────────────────────────────────────────────────
            price.setText(String.format(Locale.US, "$%.2f", product.getPrice()));
            if (product.getOriginalPrice() > product.getPrice()) {
                priceOriginal.setText(String.format(Locale.US, "$%.2f", product.getOriginalPrice()));
                priceOriginal.setPaintFlags(priceOriginal.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                priceOriginal.setVisibility(View.VISIBLE);
            } else {
                priceOriginal.setVisibility(View.GONE);
            }

            // ── Stock badge (xanh nếu đủ, đỏ nếu thấp < 15) ──────────────
            int stockQty = product.getStock();
            stock.setText(context.getString(R.string.admin_stock_format, stockQty));
            if (stockQty < 15) {
                stock.setBackgroundResource(R.drawable.bg_admin_stock_low);
                stock.setTextColor(ContextCompat.getColor(context, R.color.status_error_light));
            } else {
                stock.setBackgroundResource(R.drawable.bg_admin_stock_ok);
                stock.setTextColor(ContextCompat.getColor(context, R.color.status_success_light));
            }

            // ── Size chips (hiển thị tối đa 4, phần dư dùng +N) ───────────
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

            // ── Rating ─────────────────────────────────────────────────────
            rating.setText(String.format(Locale.US, "%.1f (%d)",
                    product.getRating(), product.getReviewCount()));

            // ── Click listeners ────────────────────────────────────────────
            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(product, position);
            });
            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(product, position);
            });
        }
    }
}