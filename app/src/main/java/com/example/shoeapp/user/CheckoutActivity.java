package com.example.shoeapp.user;

import android.content.Intent;
import android.os.Bundle;

import com.example.shoeapp.ui.BaseSoleStepActivity;
import com.example.shoeapp.ui.BottomNavHelper;
import com.example.shoeapp.R;

public class CheckoutActivity extends BaseSoleStepActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        setupScreen(BottomNavHelper.TAG_CART);

        findViewById(R.id.place_order_button).setOnClickListener(v ->
                startActivity(new Intent(this, QRPaymentActivity.class)));
    }
}
