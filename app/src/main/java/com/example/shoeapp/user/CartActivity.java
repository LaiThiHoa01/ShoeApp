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
        findViewById(R.id.checkout_button).setOnClickListener(v ->
                startActivity(new Intent(this, CheckoutActivity.class)));
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
