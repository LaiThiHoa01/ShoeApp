package com.example.shoeapp.data.model;

public class OrderItemView {
    public int orderDetailId;
    public int productId;
    public int colorId;
    public int sizeId;
    public String productName;
    public String brandName;
    public String sizeName;
    public String colorName;
    public String colorHex;
    public String imageUrl;
    public int quantity;
    public double unitPrice;

    public double subtotal() {
        return quantity * unitPrice;
    }
}
