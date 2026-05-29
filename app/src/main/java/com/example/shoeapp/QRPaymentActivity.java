package com.example.shoeapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

public class QRPaymentActivity extends BaseSoleStepActivity {
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_payment);
        setupScreen(BottomNavHelper.TAG_ORDERS);

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

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }
}
