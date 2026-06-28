package com.example.shoeapp.admin;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.shoeapp.R;

public class AdminPromotionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("Admin Promotion Management (Coming Soon)\n\nVui lòng kiểm tra màn hình Home của User để xem 3 Promotions đã được thêm vào.");
        tv.setTextColor(android.graphics.Color.WHITE);
        tv.setTextSize(18f);
        tv.setPadding(50, 50, 50, 50);
        tv.setBackgroundColor(0xFF1E1E1E);
        setContentView(tv);
    }
}
