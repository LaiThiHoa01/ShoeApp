package com.example.shoeapp.data.repo;

import android.content.Context;

import com.example.shoeapp.authentication.SessionManager;
import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.entity.User;

public class UserRepository {
    private final AppDatabase db;
    private final Context context;

    public UserRepository(Context context) {
        this.context = context.getApplicationContext();
        db = AppDatabase.getDatabase(context);
    }

    public int getCurrentUserId() {
        return SessionManager.getUserId(context);
    }

    public User getCurrentUser() {
        int id = getCurrentUserId();
        if (id == -1) return null;
        return db.userDao().getUserById(id);
    }

    public boolean isAdmin() {
        return SessionManager.isAdmin(context);
    }
}