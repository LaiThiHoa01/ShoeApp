package com.example.shoeapp.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "promotion",
        indices = {
            @Index(value = "name", unique = true),
            @Index(value = "slug", unique = true)
        })
public class Promotion {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    @ColumnInfo(name = "discount_type")
    public String discountType; // ENUM('PERCENTAGE','FIXED_AMOUNT')

    @ColumnInfo(name = "discount_value")
    public double discountValue;

    @ColumnInfo(name = "start_date")
    public String startDate;

    @ColumnInfo(name = "end_date")
    public String endDate;

    @ColumnInfo(name = "is_active")
    public boolean isActive;

    public String slug;
    
    @ColumnInfo(name = "banner_url")
    public String bannerUrl;
    public String subtitle;
    
    @ColumnInfo(name = "voucher_code")
    public String voucherCode;
    
    public String description;
    
    public int quantity;
    
    @ColumnInfo(name = "target_type")
    public String targetType; // "PRODUCTS" or "CATEGORY"
    
    @ColumnInfo(name = "category_id")
    public Integer categoryId; // Nullable
    
    @ColumnInfo(name = "brand_id")
    public Integer brandId; // Nullable
    
    @ColumnInfo(name = "max_discount_amount")
    public Double maxDiscountAmount; // Nullable
}
