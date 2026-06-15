package com.example.shoeapp.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "promotion_product",
        foreignKeys = {
            @ForeignKey(entity = Promotion.class, parentColumns = "id", childColumns = "promotion_id", onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.RESTRICT),
            @ForeignKey(entity = Product.class, parentColumns = "id", childColumns = "product_id", onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.RESTRICT)
        },
        indices = {
            @Index(value = {"promotion_id", "product_id"}, unique = true),
            @Index(value = "product_id")
        })
public class PromotionProduct {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "promotion_id")
    public int promotionId;

    @ColumnInfo(name = "product_id")
    public int productId;
}
