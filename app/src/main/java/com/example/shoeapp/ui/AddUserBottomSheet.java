package com.example.shoeapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.shoeapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddUserBottomSheet extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_close).setOnClickListener(v -> dismiss());

        View statusActive = view.findViewById(R.id.status_active);
        View statusInactive = view.findViewById(R.id.status_inactive);

        statusActive.setOnClickListener(v -> {
            statusActive.setBackgroundResource(R.drawable.bg_status_active_dark);
            statusInactive.setBackgroundResource(R.drawable.bg_card_dark);
        });

        statusInactive.setOnClickListener(v -> {
            statusInactive.setBackgroundResource(R.drawable.bg_status_active_dark); // Có thể tạo riêng bg_inactive nếu cần
            statusActive.setBackgroundResource(R.drawable.bg_card_dark);
        });

        view.findViewById(R.id.btn_create_user).setOnClickListener(v -> {
            dismiss();
        });
    }

    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialog;
    }
}
