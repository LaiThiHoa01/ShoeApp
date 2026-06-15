package com.example.shoeapp.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "tokentype", indices = {@Index(value = "token", unique = true)})
public class TokenType {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String email;
    public String token;

    @ColumnInfo(name = "token_type")
    public String tokenType;

    @ColumnInfo(name = "created_at")
    public String createdAt;

    @ColumnInfo(name = "expires_at")
    public String expiresAt;

    @ColumnInfo(name = "is_used")
    public boolean isUsed;
}
