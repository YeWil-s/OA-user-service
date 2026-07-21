package com.oa.user;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashTest {

    @Test
    void generateHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("123456");

        System.out.println("========================================");
        System.out.println("COPY THIS HASH into sql/user_db_init.sql:");
        System.out.println(hash);
        System.out.println("========================================");
        System.out.println("Verify: " + encoder.matches("123456", hash));
    }
}
