package com.example.shoeapp.admin.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.R;

import java.util.List;
import java.util.Locale;

public class AdminOrderDetailAdapter extends RecyclerView.Adapter<AdminOrderDetailAdapter.ViewHolder> {

    public static class OrderDetailDisplay {
        public final String productName;
        public final String brandName;
        public final String sizeName;
        public final String colorName;
        public final double unitPrice;
        public final int quantity;
        public final double subtotal;
        public final String imageUrl;

        public OrderDetailDisplay(String productName, String brandName, String sizeName,
                                  String colorName, double unitPrice, int quantity,
                                  double subtotal, String imageUrl) {
            this.productName = productName;
            this.brandName = brandName;
            this.sizeName = sizeName;
            this.colorName = colorName;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
            this.subtotal = subtotal;
            this.imageUrl = imageUrl;
        }
    }

    private final Context context;
    private final List<OrderDetailDisplay> items;

    public AdminOrderDetailAdapter(Context context, List<OrderDetailDisplay> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_order_detail_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderDetailDisplay item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgProduct;
        final TextView tvBrand;
        final TextView tvName;
        final TextView tvSize;
        final TextView tvColor;
        final TextView tvQtyPrice;
        final TextView tvSubtotal;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.detail_prod_image);
            tvBrand = itemView.findViewById(R.id.detail_prod_brand);
            tvName = itemView.findViewById(R.id.detail_prod_name);
            tvSize = itemView.findViewById(R.id.detail_prod_size);
            tvColor = itemView.findViewById(R.id.detail_prod_color);
            tvQtyPrice = itemView.findViewById(R.id.detail_prod_qty_price);
            tvSubtotal = itemView.findViewById(R.id.detail_prod_subtotal);
        }

        void bind(OrderDetailDisplay item) {
            tvBrand.setText(item.brandName != null ? item.brandName : "SoleStep");
            tvName.setText(item.productName != null ? item.productName : "Giày");
            tvSize.setText("Kích cỡ: " + (item.sizeName != null ? item.sizeName : "—"));
            tvColor.setText(item.colorName != null ? item.colorName : "—");
            tvQtyPrice.setText(com.example.shoeapp.Helper.Helpers.formatPrice(item.unitPrice) + " x " + item.quantity);
            tvSubtotal.setText(com.example.shoeapp.Helper.Helpers.formatPrice(item.subtotal));

            // Load product image dynamically
            if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
                try {
                    imgProduct.setImageURI(Uri.parse(item.imageUrl));
                } catch (Exception e) {
                    imgProduct.setImageResource(R.drawable.ic_shoe);
                }
            } else {
                imgProduct.setImageResource(R.drawable.ic_shoe);
            }
        }
    }
}
