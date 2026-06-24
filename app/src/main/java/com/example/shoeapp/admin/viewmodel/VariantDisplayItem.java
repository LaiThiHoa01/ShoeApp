package com.example.shoeapp.admin.viewmodel;

import com.example.shoeapp.data.entity.ProductVariant;

public class VariantDisplayItem {
    public ProductVariant variant;
    public String colorName;
    public String colorHex;
    public String sizeName;

    public VariantDisplayItem(ProductVariant variant, String colorName, String colorHex, String sizeName) {
        this.variant = variant;
        this.colorName = colorName;
        this.colorHex = colorHex;
        this.sizeName = sizeName;
    }

    public VariantDisplayItem copy() {
        ProductVariant vCopy = new ProductVariant();
        vCopy.id = this.variant.id;
        vCopy.productId = this.variant.productId;
        vCopy.colorId = this.variant.colorId;
        vCopy.sizeId = this.variant.sizeId;
        vCopy.stock = this.variant.stock;
        vCopy.isDiscontinueVariant = this.variant.isDiscontinueVariant;
        return new VariantDisplayItem(vCopy, this.colorName, this.colorHex, this.sizeName);
    }
}
