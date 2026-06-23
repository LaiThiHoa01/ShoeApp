package com.example.shoeapp.model;

/**
 * Model dữ liệu đơn hàng — dùng cho AdminOrderManagementActivity.
 */
public class Order {

    public enum Status { PROCESSING, SHIPPED, DELIVERED, CANCELLED }

    private final String orderId;      // Ví dụ: "SS-10495"
    private final String customerName;
    private final double total;
    private final int    itemCount;
    private       Status status;
    private final String date;         // Ví dụ: "May 19, 2026"

    public Order(String orderId, String customerName,
                 double total, int itemCount,
                 Status status, String date) {
        this.orderId      = orderId;
        this.customerName = customerName;
        this.total        = total;
        this.itemCount    = itemCount;
        this.status       = status;
        this.date         = date;
    }

    public String getOrderId()      { return orderId; }
    public String getCustomerName() { return customerName; }
    public double getTotal()        { return total; }
    public int    getItemCount()    { return itemCount; }
    public Status getStatus()       { return status; }
    public String getDate()         { return date; }

    /** Dùng khi admin bấm "Mark as Shipped" hoặc "Mark as Delivered" */
    public void setStatus(Status status) { this.status = status; }
}