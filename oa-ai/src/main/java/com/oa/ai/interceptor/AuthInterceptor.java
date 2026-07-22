package com.oa.ai.interceptor;

import com.oa.ai.client.UserServiceClient;
import com.oa.ai.dto.UserInfoDTO;
import com.oa.common.result.Result;
import com.oa.common.result.ResultCode;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    private final UserServiceClient userServiceClient;

    public AuthInterceptor(@Lazy UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = extractToken(request);
        if (token == null) {
            response.setStatus(401);
            return false;
        }

        try {
            Result<UserInfoDTO> result = userServiceClient.getCurrentUser(token);
            if (result.getCode() != ResultCode.SUCCESS.getCode() || result.getData() == null) {
                log.warn("Auth failed via oa-user: code={}, msg={}", result.getCode(), result.getMessage());
                response.setStatus(401);
                return false;
            }

            UserInfoDTO user = result.getData();
            request.setAttribute("userId", user.getUserId());
            request.setAttribute("username", user.getUsername());
            request.setAttribute("roles", user.getRoles());
            request.setAttribute("permissions", user.getPermissions());
            request.setAttribute("deptId", user.getDeptId());
            return true;
        } catch (FeignException e) {
            log.error("Feign call to oa-user failed: status={}, msg={}", e.status(), e.getMessage());
            response.setStatus(503);
            return false;
        } catch (Exception e) {
            log.error("Auth interceptor error: {}", e.getMessage());
            response.setStatus(401);
            return false;
        }
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken;
        }
        return null;
    }
}
