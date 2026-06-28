package com.example.shoeapp.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.R;
import com.example.shoeapp.data.entity.Product;
import com.example.shoeapp.user.ImageLoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemSelectProductAdapter extends RecyclerView.Adapter<ItemSelectProductAdapter.ViewHolder> {

    private List<Product> productList = new ArrayList<>();
    private Set<Integer> selectedProductIds = new HashSet<>();
    private OnSelectionChangedListener listener;

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }

    public void setProductList(List<Product> products) {
        this.productList = products;
        notifyDataSetChanged();
    }

    public void setSelectedProductIds(Set<Integer> ids) {
        this.selectedProductIds = ids;
        notifyDataSetChanged();
    }

    public Set<Integer> getSelectedProductIds() {
        return selectedProductIds;
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_select_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvProductName.setText(product.name);
        
        // Remove previous listener to avoid unwanted triggers
        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setChecked(selectedProductIds.contains(product.id));

        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedProductIds.add(product.id);
            } else {
                selectedProductIds.remove(product.id);
            }
            if (listener != null) {
                listener.onSelectionChanged(selectedProductIds.size());
            }
        });

        holder.itemView.setOnClickListener(v -> {
            holder.cbSelect.setChecked(!holder.cbSelect.isChecked());
        });

        // Tạm thời hiển thị ảnh default, nếu có list ảnh thì implement lấy ảnh đầu tiên
        // ImageLoader.load(product.imageUrl, holder.ivProduct, R.drawable.ic_shoe);
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvProductName;
        CheckBox cbSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            cbSelect = itemView.findViewById(R.id.cb_select_product);
        }
    }
}
