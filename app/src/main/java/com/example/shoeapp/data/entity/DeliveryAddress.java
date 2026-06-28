package com.example.shoeapp.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "delivery_addresses")
public class DeliveryAddress {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String userId;

    public String phoneNumber;
    public String address;
    public boolean isDefault;
}
