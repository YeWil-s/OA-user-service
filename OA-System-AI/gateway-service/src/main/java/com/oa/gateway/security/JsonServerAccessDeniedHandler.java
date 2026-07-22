package com.oa.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.common.response.Result;
import com.oa.common.response.ResultCode;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public class JsonServerAccessDeniedHandler implements ServerAccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JsonServerAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        return write(exchange, ResultCode.FORBIDDEN);
    }

    private Mono<Void> write(ServerWebExchange exchange, ResultCode resultCode) {
        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.valueOf(resultCode.getCode()));
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(Result.fail(resultCode));
        } catch (Exception ex) {
            bytes = ("{\"code\":" + resultCode.getCode()
                    + ",\"message\":\"" + resultCode.getMessage()
                    + "\",\"data\":null}").getBytes(StandardCharsets.UTF_8);
        }
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }
}
