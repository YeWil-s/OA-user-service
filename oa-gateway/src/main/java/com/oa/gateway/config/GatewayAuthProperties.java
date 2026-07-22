package com.oa.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "gateway.auth")
public class GatewayAuthProperties {

    private boolean enabled = true;

    private List<String> whitelist = new ArrayList<>(List.of(
            "/api/user/login",
            "/api/user/health",
            "/api/notice/health",
            "/api/visual/health",
            "/actuator/**",
            "/doc.html",
            "/webjars/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/api/*/v3/api-docs/**",
            "/favicon.ico"
    ));

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(List<String> whitelist) {
        this.whitelist = whitelist == null ? new ArrayList<>() : whitelist;
    }
}
