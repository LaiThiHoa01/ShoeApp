package com.example.shoeapp.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "color", indices = {@Index(value = "name", unique = true), @Index(value = "hexcode", unique = true), @Index(value = "color_id", unique = true)})
public class Color {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String hexcode;

    @ColumnInfo(name = "color_id")
    public String colorId;
}
