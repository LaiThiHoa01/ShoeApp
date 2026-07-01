package com.example.shoeapp.Helper.HMac;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedList;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HMacUtil {
    public final static String HMACSHA256 = "HmacSHA256";
    private static byte[] HMacEncode(final String algorithm, final String key, final String data) {
        Mac macGenerator = null;
        try {
            macGenerator = Mac.getInstance(algorithm);
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
            macGenerator.init(signingKey);
        } catch (Exception ex) {
        }

        if (macGenerator == null) {
            return null;
        }

        byte[] dataByte = null;
        try {
            dataByte = data.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        return macGenerator.doFinal(dataByte);
    }
    public static String HMacHexStringEncode(final String algorithm, final String key, final String data) {
        byte[] hmacEncodeBytes = HMacEncode(algorithm, key, data);
        if (hmacEncodeBytes == null) {
            return null;
        }
        return HexStringUtil.byteArrayToHexString(hmacEncodeBytes);
    }
}
