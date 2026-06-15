package com.example.shoeapp.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "product",
        foreignKeys = {
            @ForeignKey(entity = Brand.class, parentColumns = "id", childColumns = "brand_id", onUpdate = ForeignKey.CASCADE, onDelete = ForeignKey.RESTRICT),
            @ForeignKey(entity = Category.class, parentColumns = "id", childColumns = "shoe_category", onUpdate = ForeignKey.CASCADE, onDelete = ForeignKey.RESTRICT)
        },
        indices = {
            @Index(value = "name", unique = true),
            @Index(value = "product_id", unique = true),
            @Index(value = "brand_id"),
            @Index(value = "shoe_category"),
            @Index(value = "price")
        })
public class Product {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String description;
    public double price;

    @ColumnInfo(name = "original_price")
    public double originalPrice;

    @ColumnInfo(name = "brand_id")
    public int brandId;

    @ColumnInfo(name = "shoe_category")
    public int shoeCategory;

    @ColumnInfo(name = "added_at")
    public String addedAt;

    @ColumnInfo(name = "is_discontinue")
    public boolean isDiscontinue;

    @ColumnInfo(name = "is_available")
    public boolean isAvailable;

    @ColumnInfo(name = "product_id")
    public String productId;
}
