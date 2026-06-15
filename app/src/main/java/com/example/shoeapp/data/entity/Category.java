package com.example.shoeapp.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "category", indices = {@Index(value = "name", unique = true)})
public class Category {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    @ColumnInfo(name = "icon_url")
    public String iconUrl;

    @ColumnInfo(name = "is_active")
    public boolean isActive;

    @ColumnInfo(name = "sort_order")
    public int sortOrder;

    @ColumnInfo(name = "created_at")
    public String createdAt;
}
