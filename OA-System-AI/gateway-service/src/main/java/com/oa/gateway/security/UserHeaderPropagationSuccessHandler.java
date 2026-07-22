package com.oa.gateway.security;

import com.oa.common.constant.UserHeaderConstants;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.security.web.server.WebFilterExchange;
import reactor.core.publisher.Mono;

import java.util.StringJoiner;

public class UserHeaderPropagationSuccessHandler implements ServerAuthenticationSuccessHandler {

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        GatewayAuthenticatedUser principal = (GatewayAuthenticatedUser) authentication.getPrincipal();
        ServerWebExchange exchange = webFilterExchange.getExchange();
        ServerHttpRequest request = exchange.getRequest()
                .mutate()
                .headers(headers -> {
                    headers.remove(UserHeaderConstants.USER_ID_HEADER);
                    headers.remove(UserHeaderConstants.DEPT_ID_HEADER);
                    headers.remove(UserHeaderConstants.USERNAME_HEADER);
                    headers.remove(UserHeaderConstants.ROLES_HEADER);
                    headers.remove(UserHeaderConstants.PERMISSIONS_HEADER);
                    headers.set(UserHeaderConstants.USER_ID_HEADER, String.valueOf(principal.userId()));
                    headers.set(UserHeaderConstants.DEPT_ID_HEADER, String.valueOf(principal.deptId()));
                    headers.set(UserHeaderConstants.USERNAME_HEADER, principal.username());
                    headers.set(UserHeaderConstants.ROLES_HEADER, join(principal.roles()));
                    headers.set(UserHeaderConstants.PERMISSIONS_HEADER, join(principal.permissions()));
                })
                .build();
        return webFilterExchange.getChain().filter(exchange.mutate().request(request).build());
    }

    private String join(Iterable<String> values) {
        StringJoiner joiner = new StringJoiner(",");
        if (values != null) {
            for (String value : values) {
                joiner.add(value);
            }
        }
        return joiner.toString();
    }
}
