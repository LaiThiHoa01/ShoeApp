package com.example.shoeapp.user;

import android.content.Context;

import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.dao.CartDao;
import com.example.shoeapp.data.entity.Cart;
import com.example.shoeapp.data.entity.CartItem;
import com.example.shoeapp.data.entity.User;
import com.example.shoeapp.data.model.CartItemView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ClientCartRepository {
    private static final double SHIPPING_FEE = 30000;
    private final CartDao cartDao;
    private final int userId;

    public ClientCartRepository(Context context) {
        cartDao = AppDatabase.getDatabase(context).cartDao();
        userId = com.example.shoeapp.authentication.SessionManager.getUserId(context);
    }

    public Cart getCart() {
        Cart cart = cartDao.getCartByUser(userId);
        if (cart != null) {
            return cart;
        }
        Cart newCart = new Cart();
        newCart.userId = userId;
        newCart.createdAt = "2026-06-23";
        newCart.updatedAt = "2026-06-23";
        cartDao.insertCart(newCart);
        return cartDao.getCartByUser(userId);
    }

    public void addToCart(int productId, int colorId, int sizeId, int quantity, double unitPrice) {
        Cart cart = getCart();
        CartItem existing = cartDao.getItem(cart.id, productId, colorId, sizeId);
        if (existing != null) {
            existing.quantity += quantity;
            existing.updatedAt = "2026-06-23";
            cartDao.updateItem(existing);
            return;
        }

        CartItem item = new CartItem();
        item.cartId = cart.id;
        item.productId = productId;
        item.colorId = colorId;
        item.sizeId = sizeId;
        item.quantity = quantity;
        item.unitPrice = unitPrice;
        item.createdAt = "2026-06-23";
        item.updatedAt = "2026-06-23";
        cartDao.insertItem(item);
    }

    public List<CartItemView> getItems() {
        return cartDao.getCartItems(getCart().id);
    }

    public int getQuantity() {
        return cartDao.getCartQuantity(getCart().id);
    }

    public void updateQuantity(CartItemView view, int quantity) {
        if (quantity <= 0) {
            cartDao.deleteItemById(view.cartItemId);
            return;
        }
        int stock = cartDao.getVariantStock(view.productId, view.colorId, view.sizeId);
        int nextQuantity = Math.min(quantity, Math.max(1, stock));
        CartItem item = new CartItem();
        item.id = view.cartItemId;
        item.cartId = getCart().id;
        item.productId = view.productId;
        item.colorId = view.colorId;
        item.sizeId = view.sizeId;
        item.quantity = nextQuantity;
        item.unitPrice = view.unitPrice;
        item.createdAt = "2026-06-23";
        item.updatedAt = "2026-06-23";
        cartDao.updateItem(item);
    }

    public void deleteItem(CartItemView item) {
        cartDao.deleteItemById(item.cartItemId);
    }

    public void clearCart() {
        cartDao.deleteItemsByCart(getCart().id);
    }

    public double subtotal(List<CartItemView> items) {
        double subtotal = 0;
        for (CartItemView item : items) {
            subtotal += item.subtotal();
        }
        return subtotal;
    }

    public double shipping(List<CartItemView> items) {
        return items.isEmpty() ? 0 : SHIPPING_FEE;
    }

    public double discount(List<CartItemView> items) {
        double subtotal = subtotal(items);
        return subtotal >= 3000000 ? 150000 : 0;
    }

    public double total(List<CartItemView> items) {
        return subtotal(items) + shipping(items) - discount(items);
    }

    public String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(Math.round(price)) + " đ";
    }

    private void ensureDemoUser() {
        User user = new User();
        user.id = userId;
        user.email = "khachhang@solestep.vn";
        user.passwordHash = "";
        user.phoneNumber = "0900000000";
//        user.address = "TP. Hồ Chí Minh";
        user.role = "USER";
        user.fullName = "Khách hàng";
        user.avatarUrl = "";
        user.isActive = true;
        user.createdAt = "2026-06-23";
        user.firebaseUid = "";
        user.userId = "USR-DEMO-CLIENT";
        cartDao.insertUser(user);
    }

}
