package com.example.shoeapp.user;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import com.example.shoeapp.ui.BaseSoleStepActivity;
import com.example.shoeapp.ui.BottomNavHelper;
import com.example.shoeapp.R;

import java.text.NumberFormat;
import java.util.Locale;

public class QRPaymentActivity extends BaseSoleStepActivity {
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_payment);
        setupScreen(BottomNavHelper.TAG_ORDERS);

        bindCheckoutPayload();

        TextView timerText = findViewById(R.id.qr_timer_text);
        countDownTimer = new CountDownTimer(15 * 60 * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long totalSeconds = millisUntilFinished / 1000L;
                long minutes = totalSeconds / 60L;
                long seconds = totalSeconds % 60L;
                timerText.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                timerText.setText("00:00");
            }
        };
        countDownTimer.start();

        findViewById(R.id.simulate_payment_button).setOnClickListener(v ->
                startActivity(new Intent(this, OrderDetailActivity.class)));
    }

    private void bindCheckoutPayload() {
        Intent intent = getIntent();
        if (!intent.hasExtra(CheckoutActivity.EXTRA_GRAND_TOTAL)) {
            return;
        }

        double total = intent.getDoubleExtra(CheckoutActivity.EXTRA_GRAND_TOTAL, 0);
        String orderRef = intent.getStringExtra(CheckoutActivity.EXTRA_ORDER_REF);
        String paymentMethod = intent.getStringExtra(CheckoutActivity.EXTRA_PAYMENT_METHOD);
        String displayRef = orderRef == null ? "" : orderRef;

        ((TextView) findViewById(R.id.qr_amount_text)).setText(formatPrice(total));
        ((TextView) findViewById(R.id.qr_order_id_text)).setText("Đơn hàng #" + displayRef);
        ((TextView) findViewById(R.id.qr_reference_text)).setText(displayRef);
        if ("COD".equals(paymentMethod)) {
            ((TextView) findViewById(R.id.qr_title_text)).setText("Xác nhận đơn hàng");
            ((TextView) findViewById(R.id.qr_amount_text)).setText(formatPrice(total));
        }
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(Math.round(price)) + " đ";
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }
}
