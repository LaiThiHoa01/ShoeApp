package com.example.shoeapp.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "promotion",
        indices = {
            @Index(value = "name", unique = true),
            @Index(value = "slug", unique = true)
        })
public class Promotion {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    @ColumnInfo(name = "discount_type")
    public String discountType; // ENUM('PERCENTAGE','FIXED_AMOUNT')

    @ColumnInfo(name = "discount_value")
    public double discountValue;

    @ColumnInfo(name = "start_date")
    public String startDate;

    @ColumnInfo(name = "end_date")
    public String endDate;

    @ColumnInfo(name = "is_active")
    public boolean isActive;

    public String slug;
}
