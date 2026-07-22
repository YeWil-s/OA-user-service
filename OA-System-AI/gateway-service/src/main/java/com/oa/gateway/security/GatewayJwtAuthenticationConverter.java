package com.oa.gateway.security;

import com.oa.security.constant.SecurityConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class GatewayJwtAuthenticationConverter implements ServerAuthenticationConverter {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<String> whitelist;

    public GatewayJwtAuthenticationConverter(List<String> whitelist) {
        this.whitelist = whitelist == null ? List.of() : new ArrayList<>(whitelist);
    }

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        if (isWhitelisted(path)) {
            return Mono.empty();
        }
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String authorization = headers.getFirst(SecurityConstants.AUTHORIZATION_HEADER);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            return Mono.empty();
        }
        String token = authorization.substring(SecurityConstants.TOKEN_PREFIX.length());
        return Mono.just(new UsernamePasswordAuthenticationToken(null, token));
    }

    private boolean isWhitelisted(String path) {
        for (String pattern : whitelist) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }
}
