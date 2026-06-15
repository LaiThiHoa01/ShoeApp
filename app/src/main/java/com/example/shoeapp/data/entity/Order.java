package com.example.shoeapp.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "orders",
        foreignKeys = {
            @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "user_id", onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.RESTRICT)
        },
        indices = {
            @Index(value = "orders_id", unique = true),
            @Index(value = "user_id"),
            @Index(value = "order_status"),
            @Index(value = "payment_status"),
            @Index(value = "created_at")
        })
public class Order {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "user_id")
    public int userId;

    @ColumnInfo(name = "created_at")
    public String createdAt;

    @ColumnInfo(name = "shipping_fee")
    public Double shippingFee;

    @ColumnInfo(name = "sub_total")
    public Double subTotal;

    @ColumnInfo(name = "grand_total")
    public Double grandTotal;

    @ColumnInfo(name = "shipping_address")
    public String shippingAddress;

    @ColumnInfo(name = "phone_number")
    public String phoneNumber;

    @ColumnInfo(name = "order_status")
    public String orderStatus; // ENUM('NEW','PENDING','PROCESSING','SHIPPED','DELIVERED','COMPLETED','CANCELLED')

    @ColumnInfo(name = "payment_method")
    public String paymentMethod; // ENUM('COD','MOMO')

    @ColumnInfo(name = "payment_status")
    public String paymentStatus; // ENUM('UNPAID','PAID','FAILED','REFUNDED')

    @ColumnInfo(name = "order_note")
    public String orderNote;

    @ColumnInfo(name = "shipping_status")
    public String shippingStatus; // ENUM('PENDING','SHIPPING','DELIVERED','CANCELLED','RETURNED')

    @ColumnInfo(name = "orders_id")
    public String ordersId;
}
