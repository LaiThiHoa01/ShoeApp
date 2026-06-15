package com.example.shoeapp.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "product_img",
        foreignKeys = {
            @ForeignKey(entity = Product.class, parentColumns = "id", childColumns = "product_id", onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.RESTRICT),
            @ForeignKey(entity = Color.class, parentColumns = "id", childColumns = "color_id", onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.RESTRICT)
        },
        indices = {
            @Index(value = {"product_id", "color_id", "sort_order"}, unique = true),
            @Index(value = "color_id")
        })
public class ProductImg {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "product_id")
    public int productId;

    @ColumnInfo(name = "color_id")
    public Integer colorId;

    @ColumnInfo(name = "img_url")
    public String imgUrl;

    @ColumnInfo(name = "sort_order")
    public Integer sortOrder;

    @ColumnInfo(name = "is_active")
    public boolean isActive;

    @ColumnInfo(name = "is_thumbnail")
    public boolean isThumbnail;
}
