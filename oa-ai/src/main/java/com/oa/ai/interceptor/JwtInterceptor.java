package com.oa.ai.interceptor;

import com.oa.common.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtInterceptor.class);

    private final JwtUtils jwtUtils;

    public JwtInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = extractToken(request);
        if (token == null) {
            response.setStatus(401);
            return false;
        }

        try {
            if (!jwtUtils.validateToken(token)) {
                response.setStatus(401);
                return false;
            }

            Long userId = jwtUtils.getUserId(token);
            String username = jwtUtils.getUsername(token);
            List<String> roles = jwtUtils.getRoles(token);
            List<String> permissions = jwtUtils.getPermissions(token);

            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("roles", roles);
            request.setAttribute("permissions", permissions);

            return true;
        } catch (Exception e) {
            log.warn("JWT authentication failed: {}", e.getMessage());
            response.setStatus(401);
            return false;
        }
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
