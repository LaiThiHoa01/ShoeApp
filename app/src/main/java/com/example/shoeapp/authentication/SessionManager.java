package com.example.shoeapp.authentication;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "shoeapp_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_ROLE = "role";

    public static void saveSession(Context context, int userId, String role) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_ROLE, role)
                .apply();
    }

    public static int getUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public static String getRole(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_ROLE, "");
    }

    public static boolean isAdmin(Context context) {
        return "ADMIN".equals(getRole(context));
    }

    public static void clear(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}