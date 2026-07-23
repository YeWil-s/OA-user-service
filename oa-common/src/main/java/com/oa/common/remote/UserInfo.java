package com.oa.common.remote;

import java.util.ArrayList;
import java.util.List;

public class UserInfo {
    private Long id;
    private String username;
    private String realName;
    private Long deptId;
    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public boolean isActive() { return !Integer.valueOf(0).equals(status); }
}
