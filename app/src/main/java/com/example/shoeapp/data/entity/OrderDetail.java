package com.example.shoeapp.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "order_detail",
        foreignKeys = {
            @ForeignKey(entity = Order.class, parentColumns = "id", childColumns = "order_id", onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.RESTRICT),
            @ForeignKey(entity = Product.class, parentColumns = "id", childColumns = "product_id", onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.RESTRICT),
            @ForeignKey(entity = Color.class, parentColumns = "id", childColumns = "color_id", onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.RESTRICT),
            @ForeignKey(entity = Size.class, parentColumns = "id", childColumns = "size_id", onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.RESTRICT)
        },
        indices = {
            @Index(value = {"order_id", "product_id", "color_id", "size_id"}, unique = true),
            @Index(value = "order_detail_id", unique = true),
            @Index(value = "product_id"),
            @Index(value = "color_id"),
            @Index(value = "size_id")
        })
public class OrderDetail {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "order_id")
    public int orderId;

    @ColumnInfo(name = "product_id")
    public int productId;

    @ColumnInfo(name = "color_id")
    public int colorId;

    @ColumnInfo(name = "size_id")
    public int sizeId;

    public int quantity;

    @ColumnInfo(name = "unit_price")
    public double unitPrice;

    public double subtotal;

    @ColumnInfo(name = "order_detail_id")
    public String orderDetailId;
}
