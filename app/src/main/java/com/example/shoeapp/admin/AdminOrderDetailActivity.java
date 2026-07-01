package com.example.shoeapp.admin;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.R;
import com.example.shoeapp.admin.adapter.AdminOrderDetailAdapter;
import com.example.shoeapp.admin.adapter.AdminOrderDetailAdapter.OrderDetailDisplay;
import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.entity.*;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminOrderDetailActivity extends BaseAdminActivity {

    public static final String EXTRA_ORDER_ID = "EXTRA_ORDER_ID";

    private ImageButton btnBack;
    private TextView tvOrdersId;
    private LinearLayout layoutStatusBadge;
    private ImageView imgStatusIcon;
    private TextView tvStatusText;

    private TextView tvCustName;
    private TextView tvCustPhone;
    private TextView tvCustEmail;
    private TextView tvCustAddress;
    private TextView tvCustNote;

    private RecyclerView recyclerProducts;

    private TextView tvPayMethod;
    private TextView tvPayStatus;
    private TextView tvPaySubtotal;
    private TextView tvPayShipping;
    private TextView tvPayTotal;
    private TextView tvOrderDate;

    private LinearLayout layoutActionsContainer;
    private MaterialButton btnAction;

    private AppDatabase db;
    private int orderId = -1;
    private Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_detail);

        db = AppDatabase.getDatabase(this);

        orderId = getIntent().getIntExtra(EXTRA_ORDER_ID, -1);
        if (orderId == -1) {
            Toast.makeText(this, "Không tìm thấy mã đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setupEdgeToEdge();
        bindViews();
        loadOrderData();
        setupListeners();
    }

    private void setupEdgeToEdge() {
        androidx.activity.EdgeToEdge.enable(this);
        View root = findViewById(R.id.admin_order_detail_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void bindViews() {
        btnBack = findViewById(R.id.detail_btn_back);
        tvOrdersId = findViewById(R.id.detail_orders_id);
        layoutStatusBadge = findViewById(R.id.detail_status_badge);
        imgStatusIcon = findViewById(R.id.detail_status_icon);
        tvStatusText = findViewById(R.id.detail_status_text);

        tvCustName = findViewById(R.id.cust_name);
        tvCustPhone = findViewById(R.id.cust_phone);
        tvCustEmail = findViewById(R.id.cust_email);
        tvCustAddress = findViewById(R.id.cust_address);
        tvCustNote = findViewById(R.id.cust_note);

        recyclerProducts = findViewById(R.id.detail_recycler_products);

        tvPayMethod = findViewById(R.id.pay_method);
        tvPayStatus = findViewById(R.id.pay_status);
        tvPaySubtotal = findViewById(R.id.pay_subtotal);
        tvPayShipping = findViewById(R.id.pay_shipping);
        tvPayTotal = findViewById(R.id.pay_total);
        tvOrderDate = findViewById(R.id.detail_date);

        layoutActionsContainer = findViewById(R.id.detail_actions_container);
        btnAction = findViewById(R.id.detail_btn_action);

        recyclerProducts.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadOrderData() {
        order = db.orderDao().getOrderById(orderId);
        if (order == null) {
            Toast.makeText(this, "Không tìm thấy đơn hàng trong cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvOrdersId.setText(String.format(Locale.US, "#%d — %s", order.id,
                order.ordersId != null ? order.ordersId : "N/A"));

        tvOrderDate.setText(String.format(Locale.US, "Ngày đặt: %s",
                order.createdAt != null ? order.createdAt : "N/A"));

        updateStatusBadge(order.orderStatus);

        User customer = db.userDao().getUserById(order.userId);
        if (customer != null) {
            tvCustName.setText(customer.fullName != null ? customer.fullName : "N/A");
            tvCustEmail.setText(customer.email != null ? customer.email : "N/A");
            tvCustPhone.setText(order.phoneNumber != null ? order.phoneNumber :
                    (customer.phoneNumber != null ? customer.phoneNumber : "N/A"));
        } else {
            tvCustName.setText("N/A");
            tvCustEmail.setText("N/A");
            tvCustPhone.setText(order.phoneNumber != null ? order.phoneNumber : "N/A");
        }
        tvCustAddress.setText(order.shippingAddress != null ? order.shippingAddress : "N/A");
        tvCustNote.setText(order.orderNote != null && !order.orderNote.trim().isEmpty() ?
                order.orderNote : "Không có ghi chú");

        String method = order.paymentMethod != null ? order.paymentMethod : "COD";
        if ("ZALOPAY".equalsIgnoreCase(method)) {
            tvPayMethod.setText("Ví ZaloPay");
        } else {
            tvPayMethod.setText("Thanh toán khi nhận hàng (COD)");
        }
        String pStatus = order.paymentStatus != null ? order.paymentStatus : "UNPAID";
        if ("PAID".equalsIgnoreCase(pStatus)) {
            tvPayStatus.setText("ĐÃ THANH TOÁN");
        } else if ("FAILED".equalsIgnoreCase(pStatus)) {
            tvPayStatus.setText("THẤT BẠI");
        } else {
            tvPayStatus.setText("CHƯA THANH TOÁN");
        }
        updatePaymentStatusBadge(order.paymentStatus);

        tvPaySubtotal.setText(com.example.shoeapp.Helper.Helpers.formatPrice(order.subTotal != null ? order.subTotal : 0.0));
        tvPayShipping.setText(com.example.shoeapp.Helper.Helpers.formatPrice(order.shippingFee != null ? order.shippingFee : 0.0));
        tvPayTotal.setText(com.example.shoeapp.Helper.Helpers.formatPrice(order.grandTotal != null ? order.grandTotal : 0.0));

        List<OrderDetail> details = db.orderDao().getDetailsByOrder(order.id);
        List<OrderDetailDisplay> displayItems = new ArrayList<>();

        if (details == null) details = new java.util.ArrayList<>();
        for (OrderDetail detail : details) {
            Product product = db.productDao().getProductById(detail.productId);
            String productName = (product != null) ? product.name : "Giày không xác định";
            
            String brandName = "SoleStep";
            if (product != null) {
                Brand brand = db.productDao().getBrandById(product.brandId);
                if (brand != null) {
                    brandName = brand.name;
                }
            }

            Color color = db.productDao().getColorById(detail.colorId);
            String colorName = (color != null) ? color.name : "N/A";

            Size size = db.productDao().getSizeById(detail.sizeId);
            String sizeName = (size != null) ? size.name : "N/A";

            String imageUrl = "";
            ProductImg thumbnail = db.productDao().getThumbnail(detail.productId);
            if (thumbnail != null) {
                imageUrl = thumbnail.imgUrl;
            } else {
                List<ProductImg> imgs = db.productDao().getImagesByProduct(detail.productId);
                if (imgs != null && !imgs.isEmpty()) {
                    imageUrl = imgs.get(0).imgUrl;
                }
            }

            displayItems.add(new OrderDetailDisplay(
                    productName,
                    brandName,
                    sizeName,
                    colorName,
                    detail.unitPrice,
                    detail.quantity,
                    detail.subtotal,
                    imageUrl
            ));
        }

        AdminOrderDetailAdapter adapter = new AdminOrderDetailAdapter(this, displayItems);
        recyclerProducts.setAdapter(adapter);

        setupActionButton(order.orderStatus);
    }

    private void updateStatusBadge(String status) {
        if (status == null) status = "PROCESSING";

        layoutStatusBadge.setActivated(false);
        layoutStatusBadge.setSelected(false);
        layoutStatusBadge.setEnabled(true);

        switch (status) {
            case "SHIPPED":
                layoutStatusBadge.setActivated(true);
                imgStatusIcon.setImageResource(R.drawable.ic_truck);
                tvStatusText.setText(getString(R.string.status_shipped));
                break;

            case "DELIVERED":
            case "COMPLETED":
                layoutStatusBadge.setEnabled(false);
                imgStatusIcon.setImageResource(R.drawable.ic_check_circle);
                tvStatusText.setText(getString(R.string.status_delivered));
                break;

            case "CANCELLED":
                layoutStatusBadge.setSelected(true);
                imgStatusIcon.setImageResource(R.drawable.ic_clock);
                tvStatusText.setText("ĐÃ HỦY");
                break;

            default: // NEW, PENDING, PROCESSING
                imgStatusIcon.setImageResource(R.drawable.ic_clock);
                tvStatusText.setText(getString(R.string.status_processing));
                break;
        }
    }

    private void updatePaymentStatusBadge(String paymentStatus) {
        if (paymentStatus == null) paymentStatus = "UNPAID";

        tvPayStatus.setActivated(false);
        tvPayStatus.setSelected(false);
        tvPayStatus.setEnabled(true);

        switch (paymentStatus) {
            case "PAID":
                tvPayStatus.setEnabled(false);
                break;
            case "FAILED":
                tvPayStatus.setSelected(true);
                break;
            default: // UNPAID, REFUNDED
                break;
        }
    }

    private void setupActionButton(String status) {
        if (status == null) status = "PROCESSING";

        if ("ZALOPAY".equalsIgnoreCase(order.paymentMethod) && !"PAID".equalsIgnoreCase(order.paymentStatus)) {
            if ("PROCESSING".equals(status) || "SHIPPED".equals(status)) {
                btnAction.setVisibility(View.VISIBLE);
                btnAction.setEnabled(false);
                if ("FAILED".equalsIgnoreCase(order.paymentStatus)) {
                    btnAction.setText("Thanh toán thất bại");
                } else {
                    btnAction.setText("Chờ thanh toán ZaloPay");
                }
                btnAction.setIconResource(R.drawable.ic_clock);
                btnAction.setOnClickListener(null);
                return;
            }
        }
        btnAction.setVisibility(View.VISIBLE);
        btnAction.setActivated(false);
        btnAction.setSelected(false);
        btnAction.setEnabled(true);

        switch (status) {
            case "SHIPPED":
                btnAction.setActivated(true);
                btnAction.setText(getString(R.string.admin_mark_delivered));
                btnAction.setIconResource(R.drawable.ic_check_circle);
                btnAction.setOnClickListener(v -> updateOrderStatus("DELIVERED"));
                break;

            case "DELIVERED":
            case "COMPLETED":
                btnAction.setEnabled(false);
                btnAction.setText(getString(R.string.admin_order_completed));
                btnAction.setIconResource(R.drawable.ic_check_circle);
                btnAction.setOnClickListener(null);
                break;

            case "CANCELLED":
                btnAction.setEnabled(false);
                btnAction.setSelected(true);
                btnAction.setText("Đơn hàng đã hủy");
                btnAction.setIconResource(R.drawable.ic_clock);
                btnAction.setOnClickListener(null);
                break;

            default: // NEW, PENDING, PROCESSING
                btnAction.setText(getString(R.string.admin_mark_shipped));
                btnAction.setIconResource(R.drawable.ic_truck);
                btnAction.setOnClickListener(v -> updateOrderStatus("SHIPPED"));
                break;
        }
    }

    private void updateOrderStatus(String newStatus) {
        if (order != null) {
            order.orderStatus = newStatus;
            if ("DELIVERED".equals(newStatus)) {
                order.paymentStatus = "PAID";
            }
            if ("CANCELLED".equals(newStatus)) {
                List<OrderDetail> details = db.orderDao().getDetailsByOrder(order.id);
                if (details != null) for (OrderDetail detail : details) {
                    ProductVariant variant = db.productDao().getVariant(detail.productId, detail.colorId, detail.sizeId);
                    if (variant != null) {
                        variant.stock = variant.stock + detail.quantity;
                        db.productDao().updateProductVariant(variant);
                    }
                }
            }
            db.orderDao().update(order);
            String statusStr = newStatus;
            if ("SHIPPED".equals(newStatus)) statusStr = "Đang giao hàng";
            else if ("DELIVERED".equals(newStatus)) statusStr = "Đã giao hàng";
            else if ("CANCELLED".equals(newStatus)) statusStr = "Đã hủy";
            Toast.makeText(this, "Đã cập nhật trạng thái đơn hàng thành: " + statusStr, Toast.LENGTH_SHORT).show();
            
            loadOrderData();
        }
    }
}
