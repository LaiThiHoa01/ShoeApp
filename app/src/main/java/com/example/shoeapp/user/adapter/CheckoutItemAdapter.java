package com.example.shoeapp.user.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.R;
import com.example.shoeapp.data.model.CartItemView;
import com.example.shoeapp.user.ClientCartRepository;
import com.example.shoeapp.user.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class CheckoutItemAdapter extends RecyclerView.Adapter<CheckoutItemAdapter.ViewHolder> {
    private final ClientCartRepository cartRepository;
    private final List<CartItemView> items = new ArrayList<>();

    public CheckoutItemAdapter(ClientCartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public void submit(List<CartItemView> nextItems) {
        items.clear();
        items.addAll(nextItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checkout_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItemView item = items.get(position);
        holder.brand.setText(item.brandName);
        holder.name.setText(item.productName);
        holder.options.setText("Size " + item.sizeName + " - " + item.colorName);
        holder.price.setText(cartRepository.formatPrice(item.subtotal()));
        holder.quantity.setText("x" + item.quantity);
        ImageLoader.load(item.imageUrl, holder.image, R.drawable.ic_shoe);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView brand;
        final TextView name;
        final TextView options;
        final TextView price;
        final TextView quantity;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.checkout_item_image);
            brand = itemView.findViewById(R.id.checkout_item_brand);
            name = itemView.findViewById(R.id.checkout_item_name);
            options = itemView.findViewById(R.id.checkout_item_options);
            price = itemView.findViewById(R.id.checkout_item_price);
            quantity = itemView.findViewById(R.id.checkout_item_quantity);
        }
    }
}
