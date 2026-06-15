package com.example.shoeapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.shoeapp.data.entity.Order;
import java.util.List;

@Dao
public interface OrderDao {
    @Insert
    void insert(Order order);

    @Query("SELECT * FROM orders ORDER BY created_at DESC")
    List<Order> getAllOrders();

    @Query("SELECT * FROM orders WHERE user_id = :userId")
    List<Order> getOrdersByUser(int userId);

    @Update
    void update(Order order);

    @Delete
    void delete(Order order);
}
