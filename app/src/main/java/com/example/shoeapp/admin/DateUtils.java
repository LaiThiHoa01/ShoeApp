package com.example.shoeapp.admin;

import android.annotation.SuppressLint;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    // kiểm tra sản phẩm mới
    public static boolean isProductNew(String addedAtStr) {
        if (addedAtStr == null || addedAtStr.isEmpty()) return false;
        try {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date addedDate = sdf.parse(addedAtStr);
            if (addedDate != null) {
                long diffInMillis = new Date().getTime() - addedDate.getTime();
                long days = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffInMillis);
                return days >= 0 && days <= 7;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
