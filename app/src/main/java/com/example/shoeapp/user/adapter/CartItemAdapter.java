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
import com.example.shoeapp.data.model.CartItemView;
import com.example.shoeapp.data.repo.CartRepository;
import com.example.shoeapp.user.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> {
    public interface OnCartItemActionListener {
        void onIncrease(CartItemView item);
        void onDecrease(CartItemView item);
        void onDelete(CartItemView item);
    }

    private final Context context;
    private final CartRepository cartRepository;
    private final OnCartItemActionListener listener;
    private final List<CartItemView> items = new ArrayList<>();

    public CartItemAdapter(Context context, CartRepository cartRepository, OnCartItemActionListener listener) {
        this.context = context;
        this.cartRepository = cartRepository;
        this.listener = listener;
    }

    public void submitList(List<CartItemView> nextItems) {
        items.clear();
        items.addAll(nextItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView brand;
        final TextView name;
        final TextView size;
        final View color;
        final TextView price;
        final TextView quantity;
        final View minus;
        final View plus;
        final View delete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.cart_item_image);
            brand = itemView.findViewById(R.id.cart_item_brand);
            name = itemView.findViewById(R.id.cart_item_name);
            size = itemView.findViewById(R.id.cart_item_size);
            color = itemView.findViewById(R.id.cart_item_color);
            price = itemView.findViewById(R.id.cart_item_price);
            quantity = itemView.findViewById(R.id.cart_item_quantity);
            minus = itemView.findViewById(R.id.cart_item_minus);
            plus = itemView.findViewById(R.id.cart_item_plus);
            delete = itemView.findViewById(R.id.cart_item_delete);
        }

        void bind(CartItemView item) {
            brand.setText(item.brandName);
            name.setText(item.productName);
            size.setText(item.sizeName);
            try {
                android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
                shape.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                shape.setColor(android.graphics.Color.parseColor(item.colorHex));
                shape.setStroke(2, android.graphics.Color.LTGRAY);
                color.setBackground(shape);
            } catch (Exception e) {
                color.setBackgroundResource(R.drawable.bg_home_brand_chip);
            }
            price.setText(cartRepository.formatPrice(item.subtotal()));
            quantity.setText(String.valueOf(item.quantity));
            ImageLoader.load(item.imageUrl, image, R.drawable.ic_shoe);

            minus.setOnClickListener(v -> listener.onDecrease(item));
            plus.setOnClickListener(v -> listener.onIncrease(item));
            delete.setOnClickListener(v -> listener.onDelete(item));
        }
    }
}
