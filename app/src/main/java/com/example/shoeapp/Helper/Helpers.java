package com.example.shoeapp.Helper;

import android.annotation.SuppressLint;


import com.example.shoeapp.Helper.HMac.HMacUtil;

import org.jetbrains.annotations.NotNull;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class Helpers {
    private static int transIdDefault = 1;

    @NotNull
    @SuppressLint("DefaultLocale")
     public static String getAppTransId() {
        if (transIdDefault >= 100000) {
            transIdDefault = 1;
        }

        transIdDefault += 1;
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatDateTime = new SimpleDateFormat("yyMMdd_hhmmss");
        String timeString = formatDateTime.format(new Date());
        return String.format("%s%06d", timeString, transIdDefault);
    }

    @NotNull
    public static String getMac(@NotNull String key, @NotNull String data) throws NoSuchAlgorithmException, InvalidKeyException {
        return Objects.requireNonNull(HMacUtil.HMacHexStringEncode(HMacUtil.HMACSHA256, key, data));
     }

    public static String formatPrice(double price) {
        java.text.NumberFormat formatter = java.text.NumberFormat.getNumberInstance(new java.util.Locale("vi", "VN"));
        return formatter.format(Math.round(price)) + " đ";
    }







//    kiểm tra sanr phẩm mới
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
