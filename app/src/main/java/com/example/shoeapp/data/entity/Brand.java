package com.example.shoeapp.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "brand", indices = {@Index(value = "name", unique = true), @Index(value = "prefix", unique = true)})
public class Brand {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    @ColumnInfo(name = "logo_url")
    public String logoUrl;

    @ColumnInfo(name = "is_active")
    public boolean isActive;

    public String prefix;
}
