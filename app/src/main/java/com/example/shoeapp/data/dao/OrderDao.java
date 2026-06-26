package com.example.shoeapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.shoeapp.data.entity.Order;
import com.example.shoeapp.data.entity.OrderDetail;
import com.example.shoeapp.data.model.OrderItemView;
import com.example.shoeapp.data.model.OrderView;
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
    @Query("SELECT o.*, COALESCE(SUM(od.quantity), 0) AS total_items, " +
           "(SELECT p.name FROM product p INNER JOIN order_detail od2 ON p.id = od2.product_id WHERE od2.order_id = o.id LIMIT 1) AS first_product_name, " +
           "COALESCE((SELECT pi.img_url FROM product_img pi INNER JOIN order_detail od3 ON pi.product_id = od3.product_id WHERE od3.order_id = o.id AND pi.is_active = 1 ORDER BY pi.is_thumbnail DESC, pi.sort_order ASC LIMIT 1), '') AS first_product_image " +
           "FROM orders o LEFT JOIN order_detail od ON o.id = od.order_id WHERE o.user_id = :userId GROUP BY o.id ORDER BY o.created_at DESC")
    List<OrderView> getOrdersByUserWithCount(int userId);
    @Query("SELECT * FROM orders WHERE order_status = :status ORDER BY created_at DESC")
    List<Order> getOrdersByStatus(String status);
    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1") Order getOrderById(int id);

    // ── OrderDetail ───────────────────────────────────
    @Insert void insertDetail(OrderDetail detail);
    @Update void updateDetail(OrderDetail detail);
    @Delete void deleteDetail(OrderDetail detail);
    @Query("SELECT * FROM order_detail WHERE order_id = :orderId")
    List<OrderDetail> getDetailsByOrder(int orderId);

    @Query("SELECT od.id AS orderDetailId, od.product_id AS productId, od.color_id AS colorId, od.size_id AS sizeId, p.name AS productName, b.name AS brandName, s.name AS sizeName, c.name AS colorName, c.hexcode AS colorHex, od.quantity AS quantity, od.unit_price AS unitPrice, COALESCE((SELECT img_url FROM product_img WHERE product_id = p.id AND color_id = od.color_id AND is_active = 1 ORDER BY is_thumbnail DESC, sort_order ASC LIMIT 1), (SELECT img_url FROM product_img WHERE product_id = p.id AND is_thumbnail = 1 AND is_active = 1 LIMIT 1), '') AS imageUrl FROM order_detail od INNER JOIN product p ON p.id = od.product_id INNER JOIN brand b ON b.id = p.brand_id INNER JOIN size s ON s.id = od.size_id INNER JOIN color c ON c.id = od.color_id WHERE od.order_id = :orderId ORDER BY od.id DESC")
    List<OrderItemView> getOrderItems(int orderId);
}
