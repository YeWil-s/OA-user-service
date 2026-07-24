package com.oa.user.vo;

import java.util.List;

public class LoginVO {

    private String accessToken;
    private Long userId;
    private String username;
    private String realName;
    private String avatarUrl;
    private Long deptId;
    private String deptName;
    private Long positionId;
    private String positionName;
    private String phone;
    private String email;
    private Integer gender;
    private String entryDate;
    private List<String> roles;
    private List<String> roleNames;
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
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public Long getPositionId() { return positionId; }
    public void setPositionId(Long positionId) { this.positionId = positionId; }
    public String getPositionName() { return positionName; }
    public void setPositionName(String positionName) { this.positionName = positionName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getGender() { return gender; }
    public void setGender(Integer gender) { this.gender = gender; }
    public String getEntryDate() { return entryDate; }
    public void setEntryDate(String entryDate) { this.entryDate = entryDate; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public List<String> getRoleNames() { return roleNames; }
    public void setRoleNames(List<String> roleNames) { this.roleNames = roleNames; }
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}
