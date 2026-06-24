package com.example.shoeapp.user;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.shoeapp.R;

public final class ImageLoader {
    private ImageLoader() {
    }

    public static void load(String imageUrl, ImageView target, int fallbackResId) {
        int fallback = fallbackResId == 0 ? R.drawable.ic_shoe : fallbackResId;
        
        target.setImageTintList(null);
        target.setColorFilter(null);

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            target.setImageResource(fallback);
            return;
        }

        Glide.with(target.getContext())
                .load(imageUrl.trim())
                .placeholder(fallback)
                .error(fallback)
                .into(target);
    }
}
