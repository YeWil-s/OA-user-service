package com.oa.user.vo;

import java.util.List;

public class LoginVO {

    private String accessToken;
    private Long userId;
    private String username;
    private String realName;
    private String avatarUrl;
    private List<String> roles;
    private List<String> permissions;

    public LoginVO() {}

    public LoginVO(String accessToken, Long userId, String username, String realName,
                   String avatarUrl, List<String> roles, List<String> permissions) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.username = username;
        this.realName = realName;
        this.avatarUrl = avatarUrl;
        this.roles = roles;
        this.permissions = permissions;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}
