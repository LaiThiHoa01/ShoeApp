package com.example.shoeapp.user;

import android.content.Context;

import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.dao.CartDao;
import com.example.shoeapp.data.dao.ProductDao;
import com.example.shoeapp.data.entity.Cart;
import com.example.shoeapp.data.entity.CartItem;
import com.example.shoeapp.data.entity.Promotion;
import com.example.shoeapp.data.entity.User;
import com.example.shoeapp.data.model.CartItemView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ClientCartRepository {
    private static final double SHIPPING_FEE = 30000;
    private final CartDao cartDao;
    private final ProductDao productDao;
    private final int userId;
    private String appliedPromoCode = "";

    public ClientCartRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        cartDao = db.cartDao();
        productDao = db.productDao();
        userId = com.example.shoeapp.authentication.SessionManager.getUserId(context);
    }

    public Cart getCart() {
        Cart cart = cartDao.getCartByUser(userId);
        if (cart != null) {
            return cart;
        }
        Cart newCart = new Cart();
        newCart.userId = userId;
        String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
        newCart.createdAt = currentDate;
        newCart.updatedAt = currentDate;
        cartDao.insertCart(newCart);
        return cartDao.getCartByUser(userId);
    }

    public void addToCart(int productId, int colorId, int sizeId, int quantity, double unitPrice) {
        Cart cart = getCart();
        CartItem existing = cartDao.getItem(cart.id, productId, colorId, sizeId);
        if (existing != null) {
            existing.quantity += quantity;
            existing.updatedAt = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
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
        String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
        item.createdAt = currentDate;
        item.updatedAt = currentDate;
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
        String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
        item.createdAt = currentDate;
        item.updatedAt = currentDate;
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

    public void setAppliedPromoCode(String code) {
        this.appliedPromoCode = code;
    }

    public String getAppliedPromoCode() {
        return this.appliedPromoCode;
    }

    public boolean checkAndApplyPromoCode(String code) {
        Promotion promo = productDao.getActivePromotionByVoucher(code);
        if (promo != null) {
            setAppliedPromoCode(promo.voucherCode);
            return true;
        }
        return false;
    }

    public Promotion getSuggestedPromotion(List<CartItemView> items) {
        if (items == null || items.isEmpty()) return null;
        
        List<Promotion> allPromos = productDao.getAllPromotions();
        if (allPromos == null) return null;
        
        Promotion bestPromo = null;
        double maxDiscount = 0;

        for (Promotion promo : allPromos) {
            if (promo.quantity <= 0 || promo.voucherCode == null || promo.voucherCode.isEmpty()) continue;
            
            String oldCode = this.appliedPromoCode;
            this.appliedPromoCode = promo.voucherCode;
            double calculatedDiscount = discount(items);
            this.appliedPromoCode = oldCode;
            
            if (calculatedDiscount > maxDiscount) {
                maxDiscount = calculatedDiscount;
                bestPromo = promo;
            }
        }
        
        if (bestPromo != null && !bestPromo.voucherCode.equals(this.appliedPromoCode)) {
            return bestPromo;
        }
        return null;
    }

    public double discount(List<CartItemView> items) {
        if (appliedPromoCode != null && !appliedPromoCode.isEmpty()) {
            Promotion promo = productDao.getActivePromotionByVoucher(appliedPromoCode);
            if (promo != null) {
                double eligibleSubtotal = 0;
                
                if ("CATEGORY".equalsIgnoreCase(promo.targetType)) {
                    for (CartItemView item : items) {
                        com.example.shoeapp.data.entity.Product p = productDao.getProductById(item.productId);
                        if (p != null && promo.categoryId != null && p.shoeCategory == promo.categoryId) {
                            eligibleSubtotal += item.subtotal();
                        }
                    }
                } else if ("PRODUCTS".equalsIgnoreCase(promo.targetType)) {
                    List<com.example.shoeapp.data.entity.PromotionProduct> promoProducts = productDao.getProductsByPromotion(promo.id);
                    for (CartItemView item : items) {
                        for (com.example.shoeapp.data.entity.PromotionProduct pp : promoProducts) {
                            if (item.productId == pp.productId) {
                                eligibleSubtotal += item.subtotal();
                                break;
                            }
                        }
                    }
                } else if ("BRAND".equalsIgnoreCase(promo.targetType)) {
                    for (CartItemView item : items) {
                        com.example.shoeapp.data.entity.Product p = productDao.getProductById(item.productId);
                        if (p != null && promo.brandId != null && p.brandId == promo.brandId) {
                            eligibleSubtotal += item.subtotal();
                        }
                    }
                } else {
                    eligibleSubtotal = subtotal(items);
                }

                if (eligibleSubtotal > 0) {
                    if ("PERCENTAGE".equalsIgnoreCase(promo.discountType)) {
                        double calculated = eligibleSubtotal * (promo.discountValue / 100.0);
                        if (promo.maxDiscountAmount != null && promo.maxDiscountAmount > 0) {
                            return Math.min(calculated, promo.maxDiscountAmount);
                        }
                        return calculated;
                    } else {
                        return Math.min(promo.discountValue, eligibleSubtotal);
                    }
                }
            }
        }
        return 0;
    }

    public double total(List<CartItemView> items) {
        return subtotal(items) + shipping(items) - discount(items);
    }

    public String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(Math.round(price)) + " đ";
    }



}
