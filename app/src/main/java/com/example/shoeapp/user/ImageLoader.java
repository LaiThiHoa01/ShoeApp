package com.example.shoeapp.user;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.example.shoeapp.R;

import java.net.HttpURLConnection;
import java.net.URL;

public final class ImageLoader {
    private ImageLoader() {
    }

    public static void load(String imageUrl, ImageView target, int fallbackResId) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            target.setImageResource(fallbackResId == 0 ? R.drawable.ic_shoe : fallbackResId);
            return;
        }

        target.setTag(imageUrl);
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(imageUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(6000);
                connection.setReadTimeout(6000);
                Bitmap bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (imageUrl.equals(target.getTag()) && bitmap != null) {
                        target.setImageBitmap(bitmap);
                        target.setColorFilter(null);
                    }
                });
            } catch (Exception ignored) {
                new Handler(Looper.getMainLooper()).post(() ->
                        target.setImageResource(fallbackResId == 0 ? R.drawable.ic_shoe : fallbackResId));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}
