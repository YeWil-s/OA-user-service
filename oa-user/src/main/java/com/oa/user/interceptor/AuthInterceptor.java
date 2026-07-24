package com.oa.user.interceptor;

import com.oa.common.utils.JwtUtils;
import com.oa.common.utils.RedisUtils;
import com.oa.user.mapper.SysUserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    private final JwtUtils jwtUtils;
    private final RedisUtils redisUtils;
    private final SysUserMapper sysUserMapper;

    public AuthInterceptor(JwtUtils jwtUtils, RedisUtils redisUtils, SysUserMapper sysUserMapper) {
        this.jwtUtils = jwtUtils;
        this.redisUtils = redisUtils;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = extractToken(request);
        if (token == null) {
            response.setStatus(401);
            return false;
        }

        // Check blacklist
        if (Boolean.TRUE.equals(redisUtils.hasKey(BLACKLIST_PREFIX + token))) {
            log.debug("Token is blacklisted");
            response.setStatus(401);
            return false;
        }

        // Validate JWT
        if (!jwtUtils.validateToken(token)) {
            log.debug("JWT validation failed");
            response.setStatus(401);
            return false;
        }

        // Set user attributes — query roles/permissions from DB to reflect real-time changes
        Long userId = jwtUtils.getUserId(token);
        String username = jwtUtils.getUsername(token);
        List<String> roles = sysUserMapper.selectRoleCodesByUserId(userId);
        List<String> permissions = sysUserMapper.selectPermissionCodesByUserId(userId);

        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("roles", roles);
        request.setAttribute("permissions", permissions);
        return true;
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
