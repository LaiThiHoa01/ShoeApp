package com.example.shoeapp.user;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shoeapp.R;

public class PaymentNotification extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String result = intent.getStringExtra("result");
        boolean isSuccess = result != null && (result.toLowerCase().contains("thành công"));

        if (isSuccess) {
            setContentView(R.layout.activity_payment_notification);
        } else {
            setContentView(R.layout.activity_payment_failure);
            if (result != null) {
                TextView tvPaymentStatusDesc = findViewById(R.id.tvPaymentStatusDesc);
                if (tvPaymentStatusDesc != null) {
                    tvPaymentStatusDesc.setText(result);
                }
            }
        }


        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intentOrders = new Intent(PaymentNotification.this, MyOrdersActivity.class);
            intentOrders.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentOrders);
            finish();
        }, 2000);
    }
}