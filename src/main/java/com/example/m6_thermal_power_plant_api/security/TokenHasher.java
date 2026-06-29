package com.example.m6_thermal_power_plant_api.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class TokenHasher {
    private TokenHasher() {}

    // Refresh token đã là JWT random 256-bit → SHA-256 deterministic là đủ.
    // KHÔNG cần BCrypt (slow hash) vì entropy của token đã cao.
    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 không khả dụng trên JVM này", e);
        }
    }
}
