package com.example.shoeapp.data.repo;

import android.content.Context;

import com.example.shoeapp.authentication.SessionManager;
import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.entity.Order;
import com.example.shoeapp.data.entity.OrderDetail;
import com.example.shoeapp.data.model.CartItemView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.example.shoeapp.data.entity.ProductVariant;

public class OrderRepository {
    private final AppDatabase db;
    private final int userId;

    public OrderRepository(Context context) {
        db = AppDatabase.getDatabase(context);
        userId = SessionManager.getUserId(context);
    }

    public void saveOrder(List<CartItemView> items, double deliveryFee, double subtotal, double total,
                          String customerName, String shippingAddress, String phoneNumber, String paymentMethod, String paymentStatus, String note) {
        Order order = new Order();
        order.userId = this.userId;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        order.createdAt = sdf.format(new Date());

        order.shippingFee = deliveryFee;
        order.subTotal = subtotal;
        order.grandTotal = total;
        order.shippingAddress = "Người nhận: " + customerName + " - " + shippingAddress;
        order.phoneNumber = phoneNumber;
        order.orderStatus = "PENDING";
        order.paymentMethod = paymentMethod;
        order.paymentStatus = paymentStatus;
        order.orderNote = note;
        order.shippingStatus = "PENDING";
        order.ordersId = "ORD-" + System.currentTimeMillis();

        db.runInTransaction(() -> {
            long orderId = db.orderDao().insert(order);

            for (int i = 0; i < items.size(); i++) {
                CartItemView item = items.get(i);
                OrderDetail detail = new OrderDetail();
                detail.orderId = (int) orderId;
                detail.productId = item.productId;
                detail.colorId = item.colorId;
                detail.sizeId = item.sizeId;
                detail.quantity = item.quantity;
                detail.unitPrice = item.unitPrice;
                detail.subtotal = item.subtotal();
                detail.orderDetailId = "ORDDET-" + orderId + "-" + i;
                db.orderDao().insertDetail(detail);

                ProductVariant variant = db.productDao().getVariant(item.productId, item.colorId, item.sizeId);
                if (variant != null) {
                    variant.stock = Math.max(0, variant.stock - item.quantity);
                    db.productDao().updateProductVariant(variant);
                }
            }
        });
    }
}
