package com.oa.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(normalizeSecret(jwtProperties.getSecret()).getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(JwtClaims jwtClaims) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtProperties.getExpirationMinutes(), ChronoUnit.MINUTES);
        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(jwtClaims.getUsername())
                .claim("userId", jwtClaims.getUserId())
                .claim("deptId", jwtClaims.getDeptId())
                .claim("username", jwtClaims.getUsername())
                .claim("roles", jwtClaims.getRoles())
                .claim("permissions", jwtClaims.getPermissions())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public JwtClaims parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(jwtProperties.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        JwtClaims jwtClaims = new JwtClaims();
        jwtClaims.setUserId(getLong(claims, "userId"));
        jwtClaims.setDeptId(getLong(claims, "deptId"));
        jwtClaims.setUsername(claims.get("username", String.class));
        jwtClaims.setRoles(getStringList(claims.get("roles")));
        jwtClaims.setPermissions(getStringList(claims.get("permissions")));
        if (claims.getExpiration() != null) {
            jwtClaims.setExpiresAt(claims.getExpiration().toInstant());
        }
        return jwtClaims;
    }

    public boolean validateToken(String token) {
        parseToken(token);
        return true;
    }

    private static String normalizeSecret(String secret) {
        String value = secret == null ? "" : secret;
        if (value.length() >= 32) {
            return value;
        }
        return (value + "00000000000000000000000000000000").substring(0, 32);
    }

    private static Long getLong(Claims claims, String key) {
        Object value = claims.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private static List<String> getStringList(Object value) {
        if (!(value instanceof List<?> source)) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (Object item : source) {
            if (item != null) {
                result.add(String.valueOf(item));
            }
        }
        return result;
    }
}
