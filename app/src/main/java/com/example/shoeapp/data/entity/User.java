package com.example.shoeapp.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users",
        indices = {
            @Index(value = "email", unique = true),
            @Index(value = "user_id", unique = true),
            @Index(value = "role")
        })
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String email;

    @ColumnInfo(name = "password_hash")
    public String passwordHash;

    @ColumnInfo(name = "phone_number")
    public String phoneNumber;

    public String address;
    public String role;

    @ColumnInfo(name = "full_name")
    public String fullName;

    @ColumnInfo(name = "avatar_url")
    public String avatarUrl;

    @ColumnInfo(name = "is_active")
    public boolean isActive;

    @ColumnInfo(name = "created_at")
    public String createdAt;

    @ColumnInfo(name = "firebase_uid")
    public String firebaseUid;

    @ColumnInfo(name = "user_id")
    public String userId;
}
