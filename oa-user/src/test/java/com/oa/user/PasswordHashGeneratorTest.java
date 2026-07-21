package com.oa.user;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGeneratorTest {

    @Test
    public void generateAdminPasswordHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "123456";
        String hash = encoder.encode(rawPassword);

        System.out.println("========================================");
        System.out.println("Raw password: " + rawPassword);
        System.out.println("BCrypt hash:  " + hash);
        System.out.println("========================================");

        // Verify it matches
        boolean matches = encoder.matches(rawPassword, hash);
        System.out.println("Verification: " + (matches ? "PASS" : "FAIL"));
    }
}
