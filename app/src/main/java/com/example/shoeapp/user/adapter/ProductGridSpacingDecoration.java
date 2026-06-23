package com.example.shoeapp.user.adapter;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ProductGridSpacingDecoration extends RecyclerView.ItemDecoration {
    private final int horizontal;
    private final int vertical;

    public ProductGridSpacingDecoration(int horizontal, int vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        boolean leftColumn = position % 2 == 0;
        outRect.left = leftColumn ? 0 : horizontal / 2;
        outRect.right = leftColumn ? horizontal / 2 : 0;
        outRect.bottom = vertical;
    }
}
