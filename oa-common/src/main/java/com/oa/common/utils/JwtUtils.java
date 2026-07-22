package com.oa.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);
    private static final String DEFAULT_SECRET = "YourSuperSecretKeyForJWT_MustBeAtLeast256BitsLong!";

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtUtils(String secret, long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor((secret != null ? secret : DEFAULT_SECRET).getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public JwtUtils(long expirationMs) {
        this(DEFAULT_SECRET, expirationMs);
    }


    public String generateToken(Long userId, Long deptId, String username, List<String> roles, List<String> permissions) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        HashMap<String, Object> claims = new HashMap<>();
        claims.put("roles", roles == null ? List.of() : roles);
        claims.put("permissions", permissions == null ? List.of() : permissions);
        if (deptId != null) {
            claims.put("deptId", deptId);
        }

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .id(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Long getUserId(String token) {
        return Long.valueOf(parseToken(token).getId());
    }

    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        return parseToken(token).get("roles", List.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> getPermissions(String token) {
        return parseToken(token).get("permissions", List.class);
    }

    public Long getDeptId(String token) {
        Object deptId = parseToken(token).get("deptId");
        if (deptId == null) {
            return null;
        }
        if (deptId instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(deptId));
    }

    public long getRemainingTtl(String token) {
        Date expiration = parseToken(token).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }
}
