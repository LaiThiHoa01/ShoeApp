package com.example.shoeapp.admin.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.model.Category;
import com.example.shoeapp.R;

import java.util.List;
import java.util.Objects;


public class AdminCategoryAdapter
        extends ListAdapter<Category, AdminCategoryAdapter.ViewHolder> {

    public interface OnCategoryActionListener {
        void onEditClick(Category category, int position);
        void onToggleActiveClick(Category category, boolean isActive, int position);
        void onViewAllClick(Category category, int position);
    }

    private static final DiffUtil.ItemCallback<Category> DIFF_CALLBACK = new DiffUtil.ItemCallback<Category>() {
        @Override
        public boolean areItemsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
            return oldItem.getId() == newItem.getId()
                    && Objects.equals(oldItem.getName(), newItem.getName())
                    && oldItem.getIconResId() == newItem.getIconResId()
                    && oldItem.getIconBgColorRes() == newItem.getIconBgColorRes()
                    && oldItem.getAccentColorRes() == newItem.getAccentColorRes()
                    && oldItem.getProductCount() == newItem.getProductCount()
                    && oldItem.getMaxProducts() == newItem.getMaxProducts()
                    && oldItem.isActive() == newItem.isActive();
        }
    };

    private final Context                   context;
    private final OnCategoryActionListener  listener;

    public AdminCategoryAdapter(Context context,
                                OnCategoryActionListener listener) {
        super(DIFF_CALLBACK);
        this.context    = context;
        this.listener   = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_admin_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = getItem(position);
        holder.bind(category, position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        final FrameLayout   iconBg;
        final ImageView     icon;
        final TextView      name;

        final TextView      productCount;

        final TextView      viewAll;
        final ImageButton   btnEdit;
        final androidx.appcompat.widget.SwitchCompat catSwitch;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconBg       = itemView.findViewById(R.id.admin_cat_icon_bg);
            icon         = itemView.findViewById(R.id.admin_cat_icon);
            name         = itemView.findViewById(R.id.admin_cat_name);

            productCount = itemView.findViewById(R.id.admin_cat_product_count);

            viewAll      = itemView.findViewById(R.id.admin_cat_view_all);
            btnEdit      = itemView.findViewById(R.id.admin_cat_btn_edit);
            catSwitch    = itemView.findViewById(R.id.admin_cat_switch);
        }

        void bind(Category category, int position) {
            // ── Accent color (dot + progress + icon bg) ────────────────────
            int accentColor = ContextCompat.getColor(context, category.getAccentColorRes());
            ColorStateList accentTint = ColorStateList.valueOf(accentColor);

            // ── Icon bg tint (mờ hơn accent — 15%) ────────────────────────
            int bgColor = ContextCompat.getColor(context, category.getIconBgColorRes());
            iconBg.setBackgroundTintList(ColorStateList.valueOf(bgColor));

            // ── Icon ───────────────────────────────────────────────────────
            if (category.getIconUrl() != null && !category.getIconUrl().isEmpty()) {
                com.bumptech.glide.Glide.with(context)
                        .load(category.getIconUrl())
                        .placeholder(R.drawable.ic_shoe)
                        .error(R.drawable.ic_shoe)
                        .into(icon);
            } else {
                icon.setImageResource(category.getIconResId());
            }

            // ── Tên danh mục ───────────────────────────────────────────────
            name.setText(category.getName());

            // ── Active State Style ────────────────────────────────────────
            if (category.isActive()) {
                itemView.setAlpha(1.0f);
            } else {
                itemView.setAlpha(0.5f);
            }

            // ── Số sản phẩm ────────────────────────────────────────────────
            int count = category.getProductCount();
            productCount.setText(context.getResources()
                    .getQuantityString(R.plurals.admin_products_count, count, count));



            // ── Click: View all ────────────────────────────────────────────
            viewAll.setOnClickListener(v -> {
                if (listener != null) listener.onViewAllClick(category, position);
            });

            // ── Click: Edit ────────────────────────────────────────────────
            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(category, position);
            });

            // ── Toggle Switch Listener ──────────────────────────────────────
            catSwitch.setOnCheckedChangeListener(null);
            catSwitch.setChecked(category.isActive());
            catSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onToggleActiveClick(category, isChecked, position);
                }
            });

            // ── Click toàn card ────────────────────────────────────────────
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onViewAllClick(category, position);
            });
        }
    }
}
