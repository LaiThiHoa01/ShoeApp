package com.example.shoeapp.user;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.shoeapp.R;
import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.entity.Product;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MessageLinkifier {

    public static SpannableStringBuilder linkifyProducts(Context context, String text) {
        if (text == null) {
            return new SpannableStringBuilder("");
        }

        SpannableStringBuilder builder = new SpannableStringBuilder(text);

        AppDatabase db = AppDatabase.getDatabase(context);
        List<Product> products = db.productDao().getAllProductsActive();

        Collections.sort(products, new Comparator<Product>() {
            @Override
            public int compare(Product p1, Product p2) {
                return Integer.compare(p2.name.length(), p1.name.length());
            }
        });

        String lowerText = text.toLowerCase(Locale.getDefault());

        boolean[] matchedIndices = new boolean[text.length()];
        int brandColor = ContextCompat.getColor(context, R.color.brand_orange);

        for (Product product : products) {
            String nameLower = product.name.toLowerCase(Locale.getDefault());
            int index = 0;
            while (true) {
                index = lowerText.indexOf(nameLower, index);
                if (index == -1) {
                    break;
                }

                int end = index + nameLower.length();

                boolean alreadyMatched = false;
                for (int i = index; i < end; i++) {
                    if (matchedIndices[i]) {
                        alreadyMatched = true;
                        break;
                    }
                }

                if (!alreadyMatched) {
                    for (int i = index; i < end; i++) {
                        matchedIndices[i] = true;
                    }

                    final int productId = product.id;
                    ClickableSpan clickableSpan = new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View widget) {
                            Intent intent = new Intent(context, ProductDetailActivity.class);
                            intent.putExtra("product_id", productId);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }

                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setColor(brandColor);
                            ds.setUnderlineText(true);
                            ds.setFakeBoldText(true);
                        }
                    };

                    builder.setSpan(clickableSpan, index, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                index += nameLower.length();
            }
        }

        return builder;
    }
}
