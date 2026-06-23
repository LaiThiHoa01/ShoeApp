package com.example.shoeapp.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.shoeapp.data.entity.Cart;
import com.example.shoeapp.data.entity.CartItem;
import com.example.shoeapp.data.entity.User;
import com.example.shoeapp.data.model.CartItemView;

import java.util.List;

@Dao
public interface CartDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertUser(User user);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertCart(Cart cart);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertItem(CartItem item);

    @Query("SELECT * FROM carts WHERE user_id = :userId LIMIT 1")
    Cart getCartByUser(int userId);

    @Query("SELECT * FROM cart_items WHERE cart_id = :cartId AND product_id = :productId AND color_id = :colorId AND size_id = :sizeId LIMIT 1")
    CartItem getItem(int cartId, int productId, int colorId, int sizeId);

    @Query("SELECT ci.id AS cartItemId, ci.product_id AS productId, ci.color_id AS colorId, ci.size_id AS sizeId, p.name AS productName, b.name AS brandName, s.name AS sizeName, c.name AS colorName, c.hexcode AS colorHex, ci.quantity AS quantity, ci.unit_price AS unitPrice, COALESCE((SELECT img_url FROM product_img WHERE product_id = p.id AND color_id = ci.color_id AND is_active = 1 ORDER BY is_thumbnail DESC, sort_order ASC LIMIT 1), (SELECT img_url FROM product_img WHERE product_id = p.id AND is_thumbnail = 1 AND is_active = 1 LIMIT 1), '') AS imageUrl FROM cart_items ci INNER JOIN product p ON p.id = ci.product_id INNER JOIN brand b ON b.id = p.brand_id INNER JOIN size s ON s.id = ci.size_id INNER JOIN color c ON c.id = ci.color_id WHERE ci.cart_id = :cartId ORDER BY ci.id DESC")
    List<CartItemView> getCartItems(int cartId);

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM cart_items WHERE cart_id = :cartId")
    int getCartQuantity(int cartId);

    @Query("SELECT COALESCE(stock, 0) FROM product_variant WHERE product_id = :productId AND color_id = :colorId AND size_id = :sizeId AND is_discontinue_variant = 0 LIMIT 1")
    int getVariantStock(int productId, int colorId, int sizeId);

    @Update
    void updateItem(CartItem item);

    @Delete
    void deleteItem(CartItem item);

    @Query("DELETE FROM cart_items WHERE id = :itemId")
    void deleteItemById(int itemId);
}
