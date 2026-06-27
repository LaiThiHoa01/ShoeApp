package com.example.shoeapp.data.model;

import androidx.room.Embedded;
import androidx.room.ColumnInfo;
import com.example.shoeapp.data.entity.Order;

public class OrderWithUser {
    @Embedded
    public Order order;

    @ColumnInfo(name = "user_name")
    public String userName;
}
