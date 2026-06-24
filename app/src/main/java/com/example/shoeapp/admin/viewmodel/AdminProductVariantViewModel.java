package com.example.shoeapp.admin.viewmodel;

import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.entity.*;

import java.util.ArrayList;
import java.util.List;

public class AdminProductVariantViewModel {

    public interface OnStateChangedListener {
        void onStateChanged(List<VariantDisplayItem> items);
        void onError(String message);
        void onSaveSuccess();
    }

    private final List<VariantDisplayItem> items = new ArrayList<>();
    private OnStateChangedListener listener;

    public void setListener(OnStateChangedListener listener) {
        this.listener = listener;
    }

    public List<VariantDisplayItem> getItems() {
        return items;
    }

    /** Tải danh sách biến thể từ database */
    public void loadVariants(AppDatabase db, int productId) {
        items.clear();
        List<ProductVariant> variants = db.productDao().getVariantsByProduct(productId);
        for (ProductVariant v : variants) {
            Color color = db.productDao().getColorById(v.colorId);
            Size size = db.productDao().getSizeById(v.sizeId);

            String colorName = color != null ? color.name : "N/A";
            String colorHex = color != null ? color.hexcode : "#7F7F7F";
            String sizeName = size != null ? size.name : "N/A";

            items.add(new VariantDisplayItem(v, colorName, colorHex, sizeName));
        }
        if (listener != null) {
            listener.onStateChanged(new ArrayList<>(items));
        }
    }

    /** Cập nhật số lượng tồn kho trong bộ nhớ tạm */
    public void updateStock(int position, int newStock) {
        if (position >= 0 && position < items.size()) {
            items.get(position).variant.stock = Math.max(0, newStock);
            if (listener != null) {
                listener.onStateChanged(new ArrayList<>(items));
            }
        }
    }

    /** Tăng/Giảm số lượng tồn kho */
    public void adjustStock(int position, int delta) {
        if (position >= 0 && position < items.size()) {
            ProductVariant v = items.get(position).variant;
            v.stock = Math.max(0, v.stock + delta);
            if (listener != null) {
                listener.onStateChanged(new ArrayList<>(items));
            }
        }
    }

    /** Thêm biến thể mới vào bộ nhớ tạm */
    public boolean addVariant(AppDatabase db, int productId, int colorId, int sizeId, int stock) {
        // 1. Kiểm tra xem màu/size đã tồn tại trong danh sách tạm chưa
        for (VariantDisplayItem item : items) {
            if (item.variant.colorId == colorId && item.variant.sizeId == sizeId) {
                if (listener != null) {
                    listener.onError("Biến thể màu sắc và kích thước này đã tồn tại!");
                }
                return false;
            }
        }

        // 2. Lấy thông tin màu và size từ DB để hiển thị
        Color color = db.productDao().getColorById(colorId);
        Size size = db.productDao().getSizeById(sizeId);

        String colorName = color != null ? color.name : "N/A";
        String colorHex = color != null ? color.hexcode : "#7F7F7F";
        String sizeName = size != null ? size.name : "N/A";

        // 3. Tạo thực thể mới (id = 0 để Room tự sinh tự tăng)
        ProductVariant newVariant = new ProductVariant();
        newVariant.productId = productId;
        newVariant.colorId = colorId;
        newVariant.sizeId = sizeId;
        newVariant.stock = stock;
        newVariant.isDiscontinueVariant = false;

        items.add(new VariantDisplayItem(newVariant, colorName, colorHex, sizeName));

        if (listener != null) {
            listener.onStateChanged(new ArrayList<>(items));
        }
        return true;
    }

    /** Lưu toàn bộ thay đổi tạm thời xuống Database thực tế */
    public void saveChanges(AppDatabase db) {
        try {
            List<ProductVariant> variantsToSave = new ArrayList<>();
            for (VariantDisplayItem item : items) {
                variantsToSave.add(item.variant);
            }
            db.productDao().updateProductVariantsTransaction(variantsToSave);
            if (listener != null) {
                listener.onSaveSuccess();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onError("Lỗi khi lưu dữ liệu: " + e.getMessage());
            }
        }
    }
}
