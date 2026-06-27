package com.example.shoeapp.authentication;

import android.content.Context;

import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.dao.UserDao;
import com.example.shoeapp.data.entity.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AuthRepository {
    private final UserDao userDao;

    public AuthRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        userDao = db.userDao();
    }

    public User login(String email, String password) throws AuthException {
        email = normalizeEmail(email);

        User user = userDao.getUserByEmail(email);
        if (user == null) {
            throw new AuthException("Email không tồn tại");
        }

        if (!user.isActive) {
            throw new AuthException("Tài khoản đã bị khóa");
        }

        if (!PasswordHasher.verify(password, user.passwordHash)) {
            throw new AuthException("Mật khẩu không đúng");
        }

        return user;
    }

    public User register(
            String firstName,
            String lastName,
            String email,
            String phone,
            String password
    ) throws AuthException {
        email = normalizeEmail(email);

        validateEmail(email);
        validatePassword(password);

        if (isBlank(firstName) || isBlank(lastName)) {
            throw new AuthException("Vui lòng nhập đầy đủ họ tên");
        }

        if (isBlank(phone)) {
            throw new AuthException("Vui lòng nhập số điện thoại");
        }

        if (userDao.countByEmail(email) > 0) {
            throw new AuthException("Email đã được sử dụng");
        }

        User user = new User();
        user.email = email;
        user.passwordHash = PasswordHasher.hash(password);
        user.phoneNumber = phone.trim();
        user.fullName = firstName.trim() + " " + lastName.trim();
        user.role = "CUSTOMER";
        user.avatarUrl = "";
        user.firebaseUid = "";
        user.isActive = true;
        user.createdAt = now();
        user.userId = "USR" + System.currentTimeMillis();

        userDao.insert(user);

        return userDao.getUserByEmail(email);
    }

    public void resetPassword(String email, String newPassword, String confirmPassword) throws AuthException {
        email = normalizeEmail(email);

        validateEmail(email);
        validatePassword(newPassword);

        if (!newPassword.equals(confirmPassword)) {
            throw new AuthException("Mật khẩu xác nhận không khớp");
        }

        User user = userDao.getUserByEmail(email);
        if (user == null) {
            throw new AuthException("Email không tồn tại");
        }

        userDao.updatePasswordByEmail(email, PasswordHasher.hash(newPassword));
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static void validateEmail(String email) throws AuthException {
        if (isBlank(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            throw new AuthException("Email không hợp lệ");
        }
    }

    private static void validatePassword(String password) throws AuthException {
        if (isBlank(password) || password.length() < 6) {
            throw new AuthException("Mật khẩu phải có ít nhất 6 ký tự");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    public static class AuthException extends Exception {
        public AuthException(String message) {
            super(message);
        }
    }

    public void forgotPassword(String email, String newPassword, String confirmPassword) throws AuthException {
        email = normalizeEmail(email);

        validateEmail(email);
        validatePassword(newPassword);

        if (!newPassword.equals(confirmPassword)) {
            throw new AuthException("Mật khẩu xác nhận không khớp");
        }

        User user = userDao.getUserByEmail(email);
        if (user == null) {
            throw new AuthException("Email không tồn tại");
        }

        if (!user.isActive) {
            throw new AuthException("Tài khoản đã bị khóa");
        }

        userDao.updatePasswordByEmail(email, PasswordHasher.hash(newPassword));
    }
}