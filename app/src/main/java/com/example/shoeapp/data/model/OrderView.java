package com.example.shoeapp.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

import com.example.shoeapp.data.entity.Order;

public class OrderView {
    @Embedded
    public Order order;

    @ColumnInfo(name = "total_items")
    public int totalItems;

    @ColumnInfo(name = "first_product_name")
    public String firstProductName;

    @ColumnInfo(name = "first_product_image")
    public String firstProductImage;
}
