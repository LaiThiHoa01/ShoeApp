package com.example.shoeapp.user;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.Api.CreateOrder;
import com.example.shoeapp.R;
import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.entity.User;
import com.example.shoeapp.data.model.CartItemView;
import com.example.shoeapp.ui.BaseSoleStepActivity;
import com.example.shoeapp.ui.BottomNavHelper;
import com.example.shoeapp.user.adapter.CheckoutItemAdapter;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class CheckoutActivity extends BaseSoleStepActivity {
    private static final double STANDARD_SHIPPING = 30000;
    private static final double EXPRESS_SHIPPING = 60000;
    private static final double NEXT_DAY_SHIPPING = 90000;

    private ClientCartRepository cartRepository;
    private ClientOrderRepository orderRepository;
    private CheckoutItemAdapter adapter;
    private final List<CartItemView> items = new ArrayList<>();

    private EditText nameInput;
    private EditText phoneInput;
    private EditText addressInput;
    private EditText noteInput;
    private MaterialButton placeOrderButton;
    private TextView itemCountText;
    private TextView emptyText;
    private TextView subtotalValue;
    private TextView shippingValue;
    private TextView discountValue;
    private TextView totalValue;

    private View deliveryStandard;
    private View deliveryExpress;
    private View deliveryNextDay;
    private View paymentQr;
    private View paymentCod;

    private String selectedDelivery = "Giao tiêu chuẩn";
    private String selectedPayment = "ZALOPAY";
    private double shippingFee = STANDARD_SHIPPING;

    private void bindViews() {
        nameInput = findViewById(R.id.checkout_name_input);
        phoneInput = findViewById(R.id.checkout_phone_input);
        addressInput = findViewById(R.id.checkout_address_input);
        noteInput = findViewById(R.id.checkout_note_input);
        placeOrderButton = findViewById(R.id.place_order_button);
        itemCountText = findViewById(R.id.checkout_item_count_text);
        emptyText = findViewById(R.id.checkout_empty_text);
        subtotalValue = findViewById(R.id.checkout_subtotal_value);
        shippingValue = findViewById(R.id.checkout_shipping_value);
        discountValue = findViewById(R.id.checkout_discount_value);
        totalValue = findViewById(R.id.checkout_total_value);
        deliveryStandard = findViewById(R.id.delivery_standard);
        deliveryExpress = findViewById(R.id.delivery_express);
        deliveryNextDay = findViewById(R.id.delivery_nextday);
        paymentQr = findViewById(R.id.payment_qr);
        paymentCod = findViewById(R.id.payment_cod);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        setupScreen(BottomNavHelper.TAG_CART);

        cartRepository = new ClientCartRepository(this);
        orderRepository = new ClientOrderRepository(this);
        bindViews();
        setupList();
        setupOptions();
        setupDefaults();
        refreshCheckout();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // ZaloPay SDK Init
        ZaloPaySDK.init(553, Environment.SANDBOX);

        placeOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double subtotal = cartRepository.subtotal(items);
                double deliveryFee = items.isEmpty() ? 0 : shippingFee;
                double discount = cartRepository.discount(items);
                double total = subtotal + deliveryFee - discount;
                long amountVal = Math.round(total);

                if ("COD".equals(selectedPayment)) {
                    saveOrderToDb("COD", "UNPAID");
                    cartRepository.clearCart();
                    Intent intent1 = new Intent(CheckoutActivity.this, PaymentNotification.class);
                    intent1.putExtra("result", "Đặt hàng thành công");
                    startActivity(intent1);
                    return;
                }

                CreateOrder orderApi = new CreateOrder();
                try {
                    JSONObject data = orderApi.createOrder(String.valueOf(amountVal));
                    if (data == null) {
                        Toast.makeText(CheckoutActivity.this, "Không nhận được phản hồi từ ZaloPay", Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    Log.d("ZaloPayPayment", "Response: " + data.toString());
                    String code = data.getString("returncode");
                    if (code.equals("1")) {
                        String token = data.getString("zptranstoken");
                        ZaloPaySDK.getInstance().payOrder(CheckoutActivity.this, token, "demozpdk://app",
                                new PayOrderListener() {
                                    @Override
                                    public void onPaymentSucceeded(String s, String s1, String s2) {
                                        saveOrderToDb("ZALOPAY", "PAID");
                                        cartRepository.clearCart();
                                        Intent intent1 = new Intent(CheckoutActivity.this, PaymentNotification.class);
                                        intent1.putExtra("result", "Thanh toán thành công");
                                        startActivity(intent1);
                                    }

                                    @Override
                                    public void onPaymentCanceled(String s, String s1) {
                                        Intent intent1 = new Intent(CheckoutActivity.this, PaymentNotification.class);
                                        intent1.putExtra("result", "Hủy thanh toán");
                                        startActivity(intent1);
                                    }

                                    @Override
                                    public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {
                                        Intent intent1 = new Intent(CheckoutActivity.this, PaymentNotification.class);
                                        intent1.putExtra("result", "Lỗi thanh toán");
                                        startActivity(intent1);
                                    }
                                });
                    } else {
                        String msg = data.optString("returnmessage", "Lỗi tạo đơn hàng");
                        Toast.makeText(CheckoutActivity.this, "Lỗi: " + msg, Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(CheckoutActivity.this, "Đã xảy ra lỗi: " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

    }

    private void saveOrderToDb(String paymentMethod, String paymentStatus) {
        double subtotal = cartRepository.subtotal(items);
        double deliveryFee = items.isEmpty() ? 0 : shippingFee;
        double discount = cartRepository.discount(items);
        double total = subtotal + deliveryFee - discount;

        orderRepository.saveOrder(
                items,
                deliveryFee,
                subtotal,
                total,
                addressInput.getText().toString(),
                phoneInput.getText().toString(),
                paymentMethod,
                paymentStatus,
                noteInput.getText().toString()
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCheckout();
    }

    private void setupList() {
        RecyclerView itemsList = findViewById(R.id.checkout_items_list);
        adapter = new CheckoutItemAdapter(cartRepository);
        itemsList.setLayoutManager(new LinearLayoutManager(this));
        itemsList.setAdapter(adapter);
    }

    private void setupOptions() {
        deliveryStandard.setOnClickListener(v -> selectDelivery("Giao tiêu chuẩn", STANDARD_SHIPPING));
        deliveryExpress.setOnClickListener(v -> selectDelivery("Giao nhanh", EXPRESS_SHIPPING));
        deliveryNextDay.setOnClickListener(v -> selectDelivery("Giao trong ngày", NEXT_DAY_SHIPPING));

        paymentQr.setOnClickListener(v -> selectPayment("ZALOPAY"));
        paymentCod.setOnClickListener(v -> selectPayment("COD"));
    }

    private void setupDefaults() {
        AppDatabase db = AppDatabase.getDatabase(this);
        User user = db.userDao().getUserById(ClientCartRepository.DEMO_USER_ID);
        if (user != null) {
            nameInput.setText(user.fullName);
            phoneInput.setText(user.phoneNumber);
            addressInput.setText(user.address);
        } else {
            nameInput.setText("Khách hàng");
            phoneInput.setText("0900000000");
            addressInput.setText("TP. Hồ Chí Minh");
        }
        selectDelivery(selectedDelivery, shippingFee);
        selectPayment(selectedPayment);
    }

    private void selectDelivery(String delivery, double fee) {
        selectedDelivery = delivery;
        shippingFee = fee;
        deliveryStandard.setSelected("Giao tiêu chuẩn".equals(delivery));
        deliveryExpress.setSelected("Giao nhanh".equals(delivery));
        deliveryNextDay.setSelected("Giao trong ngày".equals(delivery));
        updateTotals();
    }

    private void selectPayment(String payment) {
        selectedPayment = payment;
        paymentQr.setSelected("ZALOPAY".equals(payment));
        paymentCod.setSelected("COD".equals(payment));
    }

    private void refreshCheckout() {
        items.clear();
        items.addAll(cartRepository.getItems());
        adapter.submit(items);

        boolean isEmpty = items.isEmpty();
        emptyText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        itemCountText.setText(cartRepository.getQuantity() + " sản phẩm");
        placeOrderButton.setEnabled(!isEmpty);
        updateTotals();
    }

    private void updateTotals() {
        double subtotal = cartRepository.subtotal(items);
        double deliveryFee = items.isEmpty() ? 0 : shippingFee;
        double discount = cartRepository.discount(items);
        double total = subtotal + deliveryFee - discount;

        subtotalValue.setText(cartRepository.formatPrice(subtotal));
        shippingValue.setText(cartRepository.formatPrice(deliveryFee));
        discountValue.setText(discount > 0
                ? "-" + cartRepository.formatPrice(discount)
                : cartRepository.formatPrice(0));
        totalValue.setText(cartRepository.formatPrice(total));
        placeOrderButton.setText("Tiếp tục thanh toán - " + cartRepository.formatPrice(total));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }
}
