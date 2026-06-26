package com.example.shoeapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.shoeapp.data.entity.Order;
import com.example.shoeapp.data.entity.OrderDetail;
import java.util.List;

@Dao
public interface OrderDao {

    // ── Order ─────────────────────────────────────────
    @Insert long insert(Order order);
    @Update void update(Order order);
    @Delete void delete(Order order);
    @Query("SELECT * FROM orders ORDER BY created_at DESC") List<Order> getAllOrders();
    @Query("SELECT * FROM orders WHERE user_id = :userId ORDER BY created_at DESC")
    List<Order> getOrdersByUser(int userId);
    @Query("SELECT * FROM orders WHERE order_status = :status ORDER BY created_at DESC")
    List<Order> getOrdersByStatus(String status);
    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1") Order getOrderById(int id);

    // ── OrderDetail ───────────────────────────────────
    @Insert void insertDetail(OrderDetail detail);
    @Update void updateDetail(OrderDetail detail);
    @Delete void deleteDetail(OrderDetail detail);
    @Query("SELECT * FROM order_detail WHERE order_id = :orderId")
    List<OrderDetail> getDetailsByOrder(int orderId);
}
