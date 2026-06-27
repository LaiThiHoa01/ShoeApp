package com.example.shoeapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.shoeapp.data.entity.Cart;
import com.example.shoeapp.data.entity.CartItem;
import com.example.shoeapp.data.entity.User;
import java.util.List;

@Dao
public interface UserDao {

    // ── User ──────────────────────────────────────────
    @Insert void insert(User user);
    @Update void update(User user);
    @Delete void delete(User user);
    @Query("SELECT * FROM users") List<User> getAllUsers();
    @Query("SELECT * FROM users WHERE id = :id") User getUserById(int id);
    @Query("SELECT * FROM users WHERE email = :email") User getUserByEmail(String email);
    @Query("SELECT * FROM users WHERE role = :role") List<User> getUsersByRole(String role);
    
    @Query("SELECT COUNT(*) FROM users WHERE role = 'CUSTOMER'")
    int countCustomers();

    @Query("SELECT u.*, " +
           "(SELECT COUNT(*) FROM orders o WHERE o.user_id = u.id) as order_count, " +
           "(SELECT COALESCE(SUM(o.grand_total), 0) FROM orders o WHERE o.user_id = u.id AND o.order_status != 'CANCELLED') as spent_amount " +
           "FROM users u WHERE u.role = 'CUSTOMER'")
    List<com.example.shoeapp.data.model.UserWithStats> getAllCustomersWithStats();

    @Query("SELECT u.*, " +
           "(SELECT COUNT(*) FROM orders o WHERE o.user_id = u.id) as order_count, " +
           "(SELECT COALESCE(SUM(o.grand_total), 0) FROM orders o WHERE o.user_id = u.id AND o.order_status != 'CANCELLED') as spent_amount " +
           "FROM users u WHERE u.role = 'CUSTOMER' AND (u.full_name LIKE '%' || :query || '%' OR u.email LIKE '%' || :query || '%')")
    List<com.example.shoeapp.data.model.UserWithStats> searchCustomersWithStats(String query);

    // ── Cart ──────────────────────────────────────────
    @Insert void insertCart(Cart cart);
    @Update void updateCart(Cart cart);
    @Delete void deleteCart(Cart cart);
    @Query("SELECT * FROM carts WHERE user_id = :userId LIMIT 1")
    Cart getCartByUser(int userId);

    // ── CartItem ──────────────────────────────────────
    @Insert void insertCartItem(CartItem item);
    @Update void updateCartItem(CartItem item);
    @Delete void deleteCartItem(CartItem item);
    @Query("SELECT * FROM cart_items WHERE cart_id = :cartId")
    List<CartItem> getCartItems(int cartId);
    @Query("DELETE FROM cart_items WHERE cart_id = :cartId")
    void clearCart(int cartId);

    // ── Auth ──────────────────────────────────────
    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    int countByEmail(String email);

    @Query("UPDATE users SET password_hash = :newPasswordHash WHERE email = :email")
    void updatePasswordByEmail(String email, String newPasswordHash);

    @Query("SELECT * FROM users WHERE firebase_uid = :firebaseUid LIMIT 1")
    User getUserByFirebaseUid(String firebaseUid);


}
