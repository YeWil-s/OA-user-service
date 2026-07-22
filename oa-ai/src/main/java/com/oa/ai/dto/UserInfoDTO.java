package com.oa.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** 从 oa-user 服务获取的当前用户信息 */
public class UserInfoDTO {

    @JsonProperty("userId")
    private Long userId;

    private String username;

    private String realName;

    @JsonProperty("deptId")
    private Long deptId;

    private String avatarUrl;

    private List<String> roles;

    private List<String> permissions;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}
