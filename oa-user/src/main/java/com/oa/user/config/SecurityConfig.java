package com.oa.user.config;

import com.oa.common.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SecurityConfig {

    @Value("${jwt.secret:YourSuperSecretKeyForJWT_MustBeAtLeast256BitsLong!}")
    private String jwtSecret;

    @Value("${jwt.expiration:7200000}")
    private long jwtExpiration;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtUtils jwtUtils() {
        return new JwtUtils(jwtSecret, jwtExpiration);
    }
}
