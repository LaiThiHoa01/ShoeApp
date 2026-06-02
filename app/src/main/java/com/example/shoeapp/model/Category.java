package com.example.shoeapp.model;

/**
 * Model dữ liệu danh mục — dùng cho AdminCategoriesActivity.
 *
 * iconResId    → R.drawable.xxx (icon hiển thị trong vòng tròn)
 * iconBgColor  → màu nền vòng tròn icon (ví dụ: R.color.cat_sneakers_bg)
 * accentColor  → màu dot + progress bar (ví dụ: R.color.cat_sneakers)
 * productCount → tổng sản phẩm trong danh mục
 * maxProducts  → dùng để tính % progress bar (thường là tổng lớn nhất)
 */
public class Category {

    private int    id;
    private String name;
    private int    iconResId;
    private int    iconBgColorRes;   // R.color.xxx — nền vòng tròn icon
    private int    accentColorRes;   // R.color.xxx — dot + progress fill
    private int    productCount;
    private int    maxProducts;      // để tính % fill progress bar

    public Category(int id, String name,
                    int iconResId,
                    int iconBgColorRes,
                    int accentColorRes,
                    int productCount,
                    int maxProducts) {
        this.id              = id;
        this.name            = name;
        this.iconResId       = iconResId;
        this.iconBgColorRes  = iconBgColorRes;
        this.accentColorRes  = accentColorRes;
        this.productCount    = productCount;
        this.maxProducts     = maxProducts;
    }

    public int    getId()             { return id; }
    public String getName()           { return name; }
    public int    getIconResId()      { return iconResId; }
    public int    getIconBgColorRes() { return iconBgColorRes; }
    public int    getAccentColorRes() { return accentColorRes; }
    public int    getProductCount()   { return productCount; }
    public int    getMaxProducts()    { return maxProducts; }

    /**
     * Tỉ lệ fill progress bar: 0.0f → 1.0f
     * Ví dụ: 48 sản phẩm / 100 max = 0.48f (48%)
     */
    public float getProgressFraction() {
        if (maxProducts <= 0) return 0f;
        return Math.min(1f, (float) productCount / maxProducts);
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }

    public void setName(String name) {
        this.name = name;
    }
}
