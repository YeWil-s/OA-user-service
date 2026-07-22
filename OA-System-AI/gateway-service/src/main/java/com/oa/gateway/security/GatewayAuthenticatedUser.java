package com.oa.gateway.security;

import java.util.List;

public record GatewayAuthenticatedUser(
        Long userId,
        Long deptId,
        String username,
        List<String> roles,
        List<String> permissions
) {
}
