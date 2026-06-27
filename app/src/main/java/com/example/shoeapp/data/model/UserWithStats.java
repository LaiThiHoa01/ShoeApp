package com.example.shoeapp.data.model;

import androidx.room.Embedded;
import androidx.room.ColumnInfo;
import com.example.shoeapp.data.entity.User;

public class UserWithStats {
    @Embedded
    public User user;

    @ColumnInfo(name = "order_count")
    public int orderCount;

    @ColumnInfo(name = "spent_amount")
    public double spentAmount;
}
