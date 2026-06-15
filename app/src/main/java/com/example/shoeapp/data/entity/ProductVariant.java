package com.example.shoeapp.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "product_variant",
        foreignKeys = {
            @ForeignKey(entity = Product.class, parentColumns = "id", childColumns = "product_id", onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.RESTRICT),
            @ForeignKey(entity = Size.class, parentColumns = "id", childColumns = "size_id", onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.RESTRICT),
            @ForeignKey(entity = Color.class, parentColumns = "id", childColumns = "color_id", onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.RESTRICT)
        },
        indices = {
            @Index(value = {"product_id", "size_id", "color_id"}, unique = true),
            @Index(value = "size_id"),
            @Index(value = "color_id"),
            @Index(value = "stock")
        })
public class ProductVariant {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "product_id")
    public int productId;

    @ColumnInfo(name = "size_id")
    public int sizeId;

    @ColumnInfo(name = "color_id")
    public int colorId;

    public int stock;

    @ColumnInfo(name = "is_discontinue_variant")
    public boolean isDiscontinueVariant;
}
