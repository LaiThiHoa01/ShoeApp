package com.example.shoeapp;

import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

abstract class BaseSoleStepActivity extends AppCompatActivity {

    protected void setupScreen(String selectedBottomNavTag) {
        EdgeToEdge.enable(this);
        View root = findViewById(R.id.main);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        BottomNavHelper.setup(this, selectedBottomNavTag);

        View backButton = findViewById(R.id.back_button);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }
}
