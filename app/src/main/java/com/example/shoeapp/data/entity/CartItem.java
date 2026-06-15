package com.example.shoeapp.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "cart_items",
        foreignKeys = {
            @ForeignKey(entity = Cart.class, parentColumns = "id", childColumns = "cart_id", onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.RESTRICT),
            @ForeignKey(entity = Product.class, parentColumns = "id", childColumns = "product_id", onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.RESTRICT),
            @ForeignKey(entity = Color.class, parentColumns = "id", childColumns = "color_id", onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.RESTRICT),
            @ForeignKey(entity = Size.class, parentColumns = "id", childColumns = "size_id", onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.RESTRICT)
        },
        indices = {
            @Index(value = {"cart_id", "product_id", "color_id", "size_id"}, unique = true),
            @Index(value = "product_id"),
            @Index(value = "color_id"),
            @Index(value = "size_id")
        })
public class CartItem {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "cart_id")
    public int cartId;

    @ColumnInfo(name = "product_id")
    public int productId;

    @ColumnInfo(name = "color_id")
    public int colorId;

    @ColumnInfo(name = "size_id")
    public int sizeId;

    public int quantity;

    @ColumnInfo(name = "unit_price")
    public double unitPrice;

    @ColumnInfo(name = "created_at")
    public String createdAt;

    @ColumnInfo(name = "updated_at")
    public String updatedAt;
}
