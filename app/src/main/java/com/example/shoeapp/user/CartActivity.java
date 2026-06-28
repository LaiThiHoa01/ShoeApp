package com.example.shoeapp.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.R;
import com.example.shoeapp.data.model.CartItemView;
import com.example.shoeapp.ui.BaseSoleStepActivity;
import com.example.shoeapp.ui.BottomNavHelper;
import com.example.shoeapp.user.adapter.CartItemAdapter;

import java.util.List;

public class CartActivity extends BaseSoleStepActivity {
    private ClientCartRepository cartRepository;
    private CartItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        cartRepository = new ClientCartRepository(this);

        setupScreen(BottomNavHelper.TAG_CART);
        setupCartList();

        android.widget.EditText promoInput = findViewById(R.id.cart_promo_input);
        View applyButton = findViewById(R.id.cart_promo_apply_button);
        if (applyButton != null && promoInput != null) {
            applyButton.setOnClickListener(v -> {
                String code = promoInput.getText().toString().trim();
                if (code.isEmpty()) {
                    android.widget.Toast.makeText(this, "Vui lòng nhập mã khuyến mãi", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                if (cartRepository.checkAndApplyPromoCode(code)) {
                    refreshCart();
                    android.widget.Toast.makeText(this, "Áp dụng mã khuyến mãi thành công!", android.widget.Toast.LENGTH_SHORT).show();
                } else {
                    android.widget.Toast.makeText(this, "Mã khuyến mãi không hợp lệ!", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }

        findViewById(R.id.checkout_button).setOnClickListener(v -> {
            Intent intent = new Intent(this, CheckoutActivity.class);
            intent.putExtra("applied_promo_code", cartRepository.getAppliedPromoCode());
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCart();
    }

    private void setupCartList() {
        RecyclerView cartItems = findViewById(R.id.cart_items_list);
        adapter = new CartItemAdapter(this, cartRepository, new CartItemAdapter.OnCartItemActionListener() {
            @Override
            public void onIncrease(CartItemView item) {
                cartRepository.updateQuantity(item, item.quantity + 1);
                refreshCart();
            }

            @Override
            public void onDecrease(CartItemView item) {
                cartRepository.updateQuantity(item, item.quantity - 1);
                refreshCart();
            }

            @Override
            public void onDelete(CartItemView item) {
                cartRepository.deleteItem(item);
                refreshCart();
            }
        });
        cartItems.setLayoutManager(new LinearLayoutManager(this));
        cartItems.setAdapter(adapter);
    }

    private void refreshCart() {
        List<CartItemView> items = cartRepository.getItems();
        adapter.submitList(items);

        findViewById(R.id.cart_empty_text).setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        findViewById(R.id.cart_items_list).setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
        
        // Show suggested promotion
        com.example.shoeapp.data.entity.Promotion suggestedPromo = cartRepository.getSuggestedPromotion(items);
        View layoutSuggestedPromo = findViewById(R.id.layout_suggested_promo);
        if (suggestedPromo != null) {
            layoutSuggestedPromo.setVisibility(View.VISIBLE);
            TextView tvSuggestedPromoCode = findViewById(R.id.tv_suggested_promo_code);
            String discountStr = "PERCENTAGE".equalsIgnoreCase(suggestedPromo.discountType) 
                    ? String.format("Giảm %s%%", Math.round(suggestedPromo.discountValue))
                    : "Giảm " + cartRepository.formatPrice(suggestedPromo.discountValue);
            tvSuggestedPromoCode.setText(suggestedPromo.voucherCode + " - " + discountStr);
            
            findViewById(R.id.btn_use_suggested_promo).setOnClickListener(v -> {
                if (cartRepository.checkAndApplyPromoCode(suggestedPromo.voucherCode)) {
                    refreshCart();
                    android.widget.Toast.makeText(this, "Áp dụng mã khuyến mãi thành công!", android.widget.Toast.LENGTH_SHORT).show();
                    android.widget.EditText promoInput = findViewById(R.id.cart_promo_input);
                    if (promoInput != null) {
                        promoInput.setText(suggestedPromo.voucherCode);
                    }
                }
            });
        } else {
            layoutSuggestedPromo.setVisibility(View.GONE);
        }

        int quantity = cartRepository.getQuantity();
        ((TextView) findViewById(R.id.cart_count_badge)).setText(String.valueOf(quantity));
        ((TextView) findViewById(R.id.cart_subtotal_value)).setText(cartRepository.formatPrice(cartRepository.subtotal(items)));
        ((TextView) findViewById(R.id.cart_shipping_value)).setText(cartRepository.formatPrice(cartRepository.shipping(items)));
        ((TextView) findViewById(R.id.cart_discount_value)).setText("-" + cartRepository.formatPrice(cartRepository.discount(items)));
        ((TextView) findViewById(R.id.cart_total_value)).setText(cartRepository.formatPrice(cartRepository.total(items)));
        ((TextView) findViewById(R.id.checkout_button)).setText("Tiến hành thanh toán - " + cartRepository.formatPrice(cartRepository.total(items)));
        findViewById(R.id.checkout_button).setEnabled(!items.isEmpty());
    }
}
