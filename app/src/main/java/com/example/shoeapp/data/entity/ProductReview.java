package com.example.shoeapp.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "product_review",
        foreignKeys = {
            @ForeignKey(entity = Product.class, parentColumns = "id", childColumns = "product_id", onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE),
            @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "user_id", onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE),
            @ForeignKey(entity = Order.class, parentColumns = "id", childColumns = "order_id", onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE)
        },
        indices = {
            @Index(value = "product_id"),
            @Index(value = "user_id"),
            @Index(value = "order_id")
        })
public class ProductReview {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "product_id")
    public int productId;

    @ColumnInfo(name = "user_id")
    public int userId;

    @ColumnInfo(name = "order_id")
    public Integer orderId;

    @ColumnInfo(name = "image_url")
    public String imageUrl;

    public int rating;
    public String content;

    @ColumnInfo(name = "created_at")
    public String createdAt;

    @ColumnInfo(name = "is_verified_purchase")
    public boolean isVerifiedPurchase;
}
