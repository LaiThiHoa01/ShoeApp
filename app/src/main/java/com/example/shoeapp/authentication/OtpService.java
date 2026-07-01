package com.example.shoeapp.authentication;

import java.util.Random;

public final class OtpService {
    private static final long OTP_TTL_MS = 5 * 60 * 1000;

    private OtpService() {
    }

    public static String generateOtp() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    public static long expiryTime() {
        return System.currentTimeMillis() + OTP_TTL_MS;
    }

    public static boolean isExpired(long expiredAt) {
        return System.currentTimeMillis() > expiredAt;
    }

    public static void sendOtpAsync(String email, String otp, Callback callback) {
        new Thread(() -> {
            try {
                EmailOtp.sendOtp(email, otp);
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    public interface Callback {
        void onSuccess();
        void onError(Exception e);
    }
}