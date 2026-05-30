package com.example.shoeapp;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

final class BottomNavHelper {
    static final String TAG_HOME = "home";
    static final String TAG_SEARCH = "search";
    static final String TAG_CART = "cart";
    static final String TAG_ORDERS = "orders";
    static final String TAG_PROFILE = "profile";

    private static final String TAG_ICON_CONTAINER = "icon_container";
    private static final String TAG_ICON = "icon";
    private static final String TAG_LABEL = "label";

    private BottomNavHelper() {
    }

    static void setup(Activity activity, String selectedTag) {
        LinearLayout bottomNavigation = activity.findViewById(R.id.bottom_navigation);
        if (bottomNavigation == null) {
            return;
        }

        for (int i = 0; i < bottomNavigation.getChildCount(); i++) {
            View item = bottomNavigation.getChildAt(i);
            Object rawTag = item.getTag();
            if (!(rawTag instanceof String)) {
                continue;
            }

            String itemTag = (String) rawTag;
            boolean selected = itemTag.equals(selectedTag);
            applyState(activity, item, selected);
            item.setOnClickListener(v -> navigate(activity, itemTag));
        }
    }

    private static void applyState(Activity activity, View item, boolean selected) {
        FrameLayout iconContainer = findTaggedChild(item, TAG_ICON_CONTAINER, FrameLayout.class);
        ImageView icon = findTaggedChild(item, TAG_ICON, ImageView.class);
        TextView label = findTaggedChild(item, TAG_LABEL, TextView.class);

        int activeColor = ContextCompat.getColor(activity, R.color.brand_orange);
        int inactiveColor = ContextCompat.getColor(activity, R.color.text_muted);

        if (iconContainer != null) {
            iconContainer.setBackgroundResource(selected ? R.drawable.bg_bottom_nav_active : 0);
        }
        if (icon != null) {
            icon.setColorFilter(ContextCompat.getColor(activity, selected ? R.color.brand_white : R.color.text_muted));
        }
        if (label != null) {
            label.setTextColor(selected ? activeColor : inactiveColor);
        }
    }

    private static void navigate(Activity activity, String itemTag) {
        if (TAG_HOME.equals(itemTag)) {
            if (!(activity instanceof MainActivity)) {
                activity.startActivity(new Intent(activity, MainActivity.class));
            }
            return;
        }

        if (TAG_SEARCH.equals(itemTag)) {
            if (!(activity instanceof CatalogActivity)) {
                activity.startActivity(new Intent(activity, CatalogActivity.class));
            }
            return;
        }

        if (TAG_ORDERS.equals(itemTag)) {
            if (!(activity instanceof MyOrdersActivity)) {
                activity.startActivity(new Intent(activity, MyOrdersActivity.class));
            }
            return;
        }

        Toast.makeText(activity, "Màn hình này chưa được tạo trong phạm vi hiện tại", Toast.LENGTH_SHORT).show();
    }

    private static <T extends View> T findTaggedChild(View root, String tag, Class<T> viewClass) {
        if (tag.equals(root.getTag()) && viewClass.isInstance(root)) {
            return viewClass.cast(root);
        }
        if (!(root instanceof ViewGroup)) {
            return null;
        }

        ViewGroup group = (ViewGroup) root;
        for (int i = 0; i < group.getChildCount(); i++) {
            T match = findTaggedChild(group.getChildAt(i), tag, viewClass);
            if (match != null) {
                return match;
            }
        }
        return null;
    }
}
