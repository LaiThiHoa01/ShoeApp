package com.example.shoeapp.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_messages")
public class ChatMessage {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "session_id")
    public String sessionId;

    public String content;

    @ColumnInfo(name = "is_user")
    public boolean isUser;

    public long timestamp;

    @ColumnInfo(name = "is_error")
    public boolean isError;

    @ColumnInfo(name = "product_id")
    public Integer productId;

    @ColumnInfo(name = "feedback_rating")
    public int feedbackRating;
}
