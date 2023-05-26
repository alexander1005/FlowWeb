package com.boraydata.flowregistry.utils;

import com.boraydata.flowregistry.entity.Token;

import java.util.HashMap;
import java.util.Map;

public class TokenUtils {
    public static Map<String, Token> token = new HashMap<>();
//    public static Map<String, Long> idleTimeout = new HashMap<>();

    public static String decode(String data) {
        byte[] b = data.getBytes();
        for (int i = 0; i < b.length; i++) {
            b[i] -= 1;
        }
        return new String(b);
    }

    public static String encode(String data) {
        byte[] b = data.getBytes();
        for (int i = 0; i < b.length; i++) {
            b[i] += 1;
        }
        return new String(b);
    }

}
