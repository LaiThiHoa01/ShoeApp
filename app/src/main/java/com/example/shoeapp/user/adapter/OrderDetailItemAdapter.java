package com.example.shoeapp.user.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.R;
import com.example.shoeapp.data.model.OrderItemView;
import com.example.shoeapp.user.ImageLoader;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderDetailItemAdapter extends RecyclerView.Adapter<OrderDetailItemAdapter.ItemViewHolder> {

    private final Context context;
    private final List<OrderItemView> items = new ArrayList<>();

    public OrderDetailItemAdapter(Context context) {
        this.context = context;
    }

    public void submitList(List<OrderItemView> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_detail_product, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private final ImageView itemImage;
        private final TextView brandText;
        private final TextView nameText;
        private final TextView metaText;
        private final TextView priceText;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.order_item_image);
            brandText = itemView.findViewById(R.id.order_item_brand);
            nameText = itemView.findViewById(R.id.order_item_name);
            metaText = itemView.findViewById(R.id.order_item_meta);
            priceText = itemView.findViewById(R.id.order_item_price);
        }

        public void bind(OrderItemView item) {
            brandText.setText(item.brandName);
            nameText.setText(item.productName);
            metaText.setText("Size: " + item.sizeName + " · Màu: " + item.colorName + " · SL: " + item.quantity);

            NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            priceText.setText(formatter.format(item.subtotal()) + " đ");

            ImageLoader.load(item.imageUrl, itemImage, R.drawable.ic_shoe);
        }
    }
}
