package com.oa.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "oa.gateway.security")
public class GatewaySecurityProperties {

    private List<String> whitelist = new ArrayList<>(List.of(
            "/api/user/login",
            "/api/user/health",
            "/actuator/**",
            "/doc.html",
            "/webjars/**",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    ));

    public List<String> getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(List<String> whitelist) {
        this.whitelist = whitelist == null ? new ArrayList<>() : whitelist;
    }
}
