package com.example.m6_thermal_power_plant_api.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Simple test class that demonstrates generating BCrypt password hashes.
 * Run this test to print hashed passwords to the test output and verify
 * that the encoder can match the raw password against the generated hash.
 */
@SpringBootTest
public class PasswordHashingTest {

    @Autowired
    private PasswordEncoder passwordEncoder; // bean from SecurityConfig

    @Test
    void generateHashUsingBean() {
        String raw = "123456"; // change to the password you want to hash
        String encoded = passwordEncoder.encode(raw);
        System.out.println("Encoded (bean): " + encoded);
        // BCrypt uses a random salt so we assert using matches()
        assertTrue(passwordEncoder.matches(raw, encoded));
    }

    @Test
    void generateHashStandalone() {
        String raw = "123456"; // change to the password you want to hash
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encoded = encoder.encode(raw);
        System.out.println("Encoded (standalone): " + encoded);
        assertTrue(encoder.matches(raw, encoded));
    }
}

