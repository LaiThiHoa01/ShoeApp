package com.example.shoeapp.admin.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.R;
import com.example.shoeapp.admin.viewmodel.VariantDisplayItem;

public class AdminProductVariantAdapter extends ListAdapter<VariantDisplayItem, AdminProductVariantAdapter.ViewHolder> {

    public interface OnVariantActionListener {
        void onAdjustStock(int position, int delta);
    }

    private final Context context;
    private final OnVariantActionListener listener;

    public AdminProductVariantAdapter(Context context, OnVariantActionListener listener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<VariantDisplayItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<VariantDisplayItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull VariantDisplayItem oldItem, @NonNull VariantDisplayItem newItem) {
                    if (oldItem.variant.id > 0 && newItem.variant.id > 0) {
                        return oldItem.variant.id == newItem.variant.id;
                    }
                    return oldItem.variant.colorId == newItem.variant.colorId
                            && oldItem.variant.sizeId == newItem.variant.sizeId;
                }

                @Override
                public boolean areContentsTheSame(@NonNull VariantDisplayItem oldItem, @NonNull VariantDisplayItem newItem) {
                    return oldItem.variant.stock == newItem.variant.stock
                            && oldItem.colorName.equals(newItem.colorName)
                            && oldItem.sizeName.equals(newItem.sizeName)
                            && oldItem.colorHex.equals(newItem.colorHex);
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_product_variant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View colorDot;
        final TextView colorName;
        final TextView sizeName;
        final TextView btnMinus;
        final EditText stockInput;
        final TextView btnPlus;
        TextWatcher activeWatcher;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            colorDot = itemView.findViewById(R.id.item_variant_color_dot);
            colorName = itemView.findViewById(R.id.item_variant_color_name);
            sizeName = itemView.findViewById(R.id.item_variant_size_name);
            btnMinus = itemView.findViewById(R.id.item_variant_btn_minus);
            stockInput = itemView.findViewById(R.id.item_variant_stock_input);
            btnPlus = itemView.findViewById(R.id.item_variant_btn_plus);
        }

        void bind(VariantDisplayItem item) {
            // Xóa text watcher cũ để tránh chồng chéo khi tái sử dụng ViewHolder
            if (activeWatcher != null) {
                stockInput.removeTextChangedListener(activeWatcher);
            }

            // Gán dữ liệu màu sắc
            colorName.setText(item.colorName);
            try {
                int colorInt = android.graphics.Color.parseColor(item.colorHex);
                colorDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorInt));
            } catch (Exception e) {
                colorDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.brand_orange)
                ));
            }

            // Gán kích thước
            sizeName.setText("Size " + item.sizeName);

            // Gán số lượng tồn kho hiện tại
            stockInput.setText(String.valueOf(item.variant.stock));

            // Đăng ký TextWatcher mới cho ô nhập số lượng
            activeWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    String input = s.toString().trim();
                    if (!input.isEmpty()) {
                        try {
                            int newStock = Integer.parseInt(input);
                            if (newStock != item.variant.stock) {
                                item.variant.stock = newStock;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
            };
            stockInput.addTextChangedListener(activeWatcher);

            // Nút bấm tăng giảm nhanh số lượng
            btnMinus.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onAdjustStock(pos, -1);
                }
            });

            btnPlus.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onAdjustStock(pos, 1);
                }
            });
        }
    }
}
