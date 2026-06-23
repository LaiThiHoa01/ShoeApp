package com.example.shoeapp.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.R;
import com.example.shoeapp.data.model.CartItemView;
import com.example.shoeapp.ui.BaseSoleStepActivity;
import com.example.shoeapp.ui.BottomNavHelper;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends BaseSoleStepActivity {
    public static final String EXTRA_ORDER_REF = "order_ref";
    public static final String EXTRA_PAYMENT_METHOD = "payment_method";
    public static final String EXTRA_DELIVERY_METHOD = "delivery_method";
    public static final String EXTRA_SUBTOTAL = "subtotal";
    public static final String EXTRA_SHIPPING_FEE = "shipping_fee";
    public static final String EXTRA_DISCOUNT = "discount";
    public static final String EXTRA_GRAND_TOTAL = "grand_total";
    public static final String EXTRA_ITEM_COUNT = "item_count";
    public static final String EXTRA_CUSTOMER_NAME = "customer_name";
    public static final String EXTRA_PHONE = "phone";
    public static final String EXTRA_ADDRESS = "address";
    public static final String EXTRA_NOTE = "note";

    private static final double STANDARD_SHIPPING = 30000;
    private static final double EXPRESS_SHIPPING = 60000;
    private static final double NEXT_DAY_SHIPPING = 90000;

    private ClientCartRepository cartRepository;
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
    private View paymentCard;

    private String selectedDelivery = "Giao tiêu chuẩn";
    private String selectedPayment = "QR";
    private double shippingFee = STANDARD_SHIPPING;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        setupScreen(BottomNavHelper.TAG_CART);

        cartRepository = new ClientCartRepository(this);
        bindViews();
        setupList();
        setupOptions();
        setupDefaults();
        refreshCheckout();

        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        placeOrderButton.setOnClickListener(v -> continueToPayment());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCheckout();
    }

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
        paymentCard = findViewById(R.id.payment_card);
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

        paymentQr.setOnClickListener(v -> selectPayment("QR"));
        paymentCod.setOnClickListener(v -> selectPayment("COD"));
        paymentCard.setOnClickListener(v -> selectPayment("CARD"));
    }

    private void setupDefaults() {
        nameInput.setText("Khách hàng");
        phoneInput.setText("0900000000");
        addressInput.setText("TP. Hồ Chí Minh");
        selectDelivery(selectedDelivery, shippingFee);
        selectPayment(selectedPayment);
    }

    private void selectDelivery(String delivery, double fee) {
        selectedDelivery = delivery;
        shippingFee = fee;
        deliveryStandard.setBackgroundResource("Giao tiêu chuẩn".equals(delivery)
                ? R.drawable.bg_checkout_selected : R.drawable.bg_checkout_option);
        deliveryExpress.setBackgroundResource("Giao nhanh".equals(delivery)
                ? R.drawable.bg_checkout_selected : R.drawable.bg_checkout_option);
        deliveryNextDay.setBackgroundResource("Giao trong ngày".equals(delivery)
                ? R.drawable.bg_checkout_selected : R.drawable.bg_checkout_option);
        updateTotals();
    }

    private void selectPayment(String payment) {
        selectedPayment = payment;
        paymentQr.setBackgroundResource("QR".equals(payment)
                ? R.drawable.bg_checkout_selected : R.drawable.bg_checkout_option);
        paymentCod.setBackgroundResource("COD".equals(payment)
                ? R.drawable.bg_checkout_selected : R.drawable.bg_checkout_option);
        paymentCard.setBackgroundResource("CARD".equals(payment)
                ? R.drawable.bg_checkout_selected : R.drawable.bg_checkout_option);
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

    private void continueToPayment() {
        if (items.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng đang trống", Toast.LENGTH_SHORT).show();
            return;
        }
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String note = noteInput.getText().toString().trim();

        if (name.isEmpty()) {
            nameInput.requestFocus();
            nameInput.setError("Vui lòng nhập họ tên");
            return;
        }
        if (phone.isEmpty()) {
            phoneInput.requestFocus();
            phoneInput.setError("Vui lòng nhập số điện thoại");
            return;
        }
        if (address.isEmpty()) {
            addressInput.requestFocus();
            addressInput.setError("Vui lòng nhập địa chỉ giao hàng");
            return;
        }

        double subtotal = cartRepository.subtotal(items);
        double deliveryFee = shippingFee;
        double discount = cartRepository.discount(items);
        double total = subtotal + deliveryFee - discount;
        String orderRef = "SS" + System.currentTimeMillis();

        Intent intent = new Intent(this, QRPaymentActivity.class);
        intent.putExtra(EXTRA_ORDER_REF, orderRef);
        intent.putExtra(EXTRA_CUSTOMER_NAME, name);
        intent.putExtra(EXTRA_PHONE, phone);
        intent.putExtra(EXTRA_ADDRESS, address);
        intent.putExtra(EXTRA_NOTE, note);
        intent.putExtra(EXTRA_DELIVERY_METHOD, selectedDelivery);
        intent.putExtra(EXTRA_PAYMENT_METHOD, selectedPayment);
        intent.putExtra(EXTRA_SUBTOTAL, subtotal);
        intent.putExtra(EXTRA_SHIPPING_FEE, deliveryFee);
        intent.putExtra(EXTRA_DISCOUNT, discount);
        intent.putExtra(EXTRA_GRAND_TOTAL, total);
        intent.putExtra(EXTRA_ITEM_COUNT, cartRepository.getQuantity());
        startActivity(intent);
    }
}
