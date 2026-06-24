package com.example.shoeapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.shoeapp.data.entity.ChatMessage;

import java.util.List;

@Dao
public interface ChatMessageDao {
    @Insert
    void insert(ChatMessage message);

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    List<ChatMessage> getAllMessages();

    @Query("DELETE FROM chat_messages")
    void deleteAllMessages();

    @androidx.room.Delete
    void delete(ChatMessage message);

    @androidx.room.Update
    void update(ChatMessage message);
}
