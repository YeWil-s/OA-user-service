package com.oa.user.security;

import java.util.List;

public class LoginUserDetails {

    private final Long userId;
    private final Long deptId;
    private final String username;
    private final String password;
    private final List<String> roles;
    private final List<String> permissions;

    public LoginUserDetails(
            Long userId,
            Long deptId,
            String username,
            String password,
            List<String> roles,
            List<String> permissions
    ) {
        this.userId = userId;
        this.deptId = deptId;
        this.username = username;
        this.password = password;
        this.roles = roles;
        this.permissions = permissions;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getDeptId() {
        return deptId;
    }

    public List<String> getRoles() {
        return roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }
}
