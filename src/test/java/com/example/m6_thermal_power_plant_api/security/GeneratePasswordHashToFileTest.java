package com.example.m6_thermal_power_plant_api.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test that generates a BCrypt hash for the password "123456" and
 * writes the resulting hash to build/generated-password-hash.txt so you
 * can copy it from the workspace after the test runs.
 */
public class GeneratePasswordHashToFileTest {

    @Test
    void writeHashToFile() throws Exception {
        String raw = "123456";
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String encoded = encoder.encode(raw);

        Path out = Path.of("build", "generated-password-hash.txt");
        Files.createDirectories(out.getParent());
        Files.writeString(out, encoded, StandardCharsets.UTF_8);

        // Sanity-check that the encoder can verify the password
        assertTrue(encoder.matches(raw, encoded));
    }
}

