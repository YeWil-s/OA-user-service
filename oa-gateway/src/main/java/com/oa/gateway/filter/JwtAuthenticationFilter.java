package com.oa.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.common.constant.UserHeaderConstants;
import com.oa.common.result.Result;
import com.oa.common.result.ResultCode;
import com.oa.common.utils.JwtUtils;
import com.oa.gateway.config.GatewayAuthProperties;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final JwtUtils jwtUtils;
    private final GatewayAuthProperties authProperties;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, GatewayAuthProperties authProperties, ObjectMapper objectMapper) {
        this.jwtUtils = jwtUtils;
        this.authProperties = authProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().pathWithinApplication().value();
        if (!authProperties.isEnabled() || HttpMethod.OPTIONS.equals(request.getMethod()) || isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        String token = resolveToken(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        if (!StringUtils.hasText(token)) {
            return unauthorized(exchange, ResultCode.UNAUTHORIZED.getMessage());
        }

        try {
            Claims claims = jwtUtils.parseToken(token);
            String userId = claims.getId();
            if (!StringUtils.hasText(userId)) {
                return unauthorized(exchange, ResultCode.UNAUTHORIZED.getMessage());
            }

            ServerHttpRequest authenticatedRequest = request.mutate()
                    .headers(headers -> writeUserHeaders(headers, claims))
                    .build();
            return chain.filter(exchange.mutate().request(authenticatedRequest).build());
        } catch (Exception ex) {
            return unauthorized(exchange, ResultCode.UNAUTHORIZED.getMessage());
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private boolean isWhitelisted(String path) {
        for (String pattern : authProperties.getWhitelist()) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private String resolveToken(String authorization) {
        if (!StringUtils.hasText(authorization)) {
            return null;
        }
        String value = authorization.trim();
        if (value.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return value.substring(BEARER_PREFIX.length()).trim();
        }
        return value;
    }

    private void writeUserHeaders(HttpHeaders headers, Claims claims) {
        headers.remove(UserHeaderConstants.USER_ID_HEADER);
        headers.remove(UserHeaderConstants.DEPT_ID_HEADER);
        headers.remove(UserHeaderConstants.USERNAME_HEADER);
        headers.remove(UserHeaderConstants.ROLES_HEADER);
        headers.remove(UserHeaderConstants.PERMISSIONS_HEADER);

        headers.set(UserHeaderConstants.USER_ID_HEADER, claims.getId());
        setIfPresent(headers, UserHeaderConstants.DEPT_ID_HEADER, getClaimAsString(claims, "deptId"));
        setIfPresent(headers, UserHeaderConstants.USERNAME_HEADER, claims.getSubject());
        headers.set(UserHeaderConstants.ROLES_HEADER, join(getStringList(claims.get("roles"))));
        headers.set(UserHeaderConstants.PERMISSIONS_HEADER, join(getStringList(claims.get("permissions"))));
    }

    private void setIfPresent(HttpHeaders headers, String name, String value) {
        if (StringUtils.hasText(value)) {
            headers.set(name, value);
        }
    }

    private String getClaimAsString(Claims claims, String name) {
        Object value = claims.get(name);
        return value == null ? null : String.valueOf(value);
    }

    private List<String> getStringList(Object value) {
        if (value instanceof List<?> items) {
            return items.stream()
                    .filter(item -> item != null && StringUtils.hasText(String.valueOf(item)))
                    .map(String::valueOf)
                    .toList();
        }
        if (value instanceof String text && StringUtils.hasText(text)) {
            return Arrays.stream(text.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .toList();
        }
        return List.of();
    }

    private String join(List<String> values) {
        StringJoiner joiner = new StringJoiner(",");
        for (String value : values) {
            joiner.add(value);
        }
        return joiner.toString();
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(Result.fail(ResultCode.UNAUTHORIZED, message));
        } catch (Exception ex) {
            bytes = ("{\"code\":401,\"message\":\"未登录或Token已过期\",\"data\":null}")
                    .getBytes(StandardCharsets.UTF_8);
        }
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }
}
