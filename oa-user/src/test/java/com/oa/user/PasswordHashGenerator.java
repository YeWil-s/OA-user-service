package com.oa.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Run: mvn compile exec:java -pl oa-user -Dexec.mainClass="com.oa.user.PasswordHashGenerator"
 * Or just run main() in your IDE.
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("123456");

        System.out.println("========================================");
        System.out.println("BCrypt hash for '123456':");
        System.out.println(hash);
        System.out.println("========================================");
        System.out.println("Verify: " + encoder.matches("123456", hash));
    }
}
