package com.oa.user.dto;

import jakarta.validation.constraints.NotBlank;

public class DeptDTO {

    @NotBlank(message = "部门名称不能为空")
    private String deptName;
    @NotBlank(message = "部门编码不能为空")
    private String deptCode;
    private Long parentId;
    private Long leaderId;
    private Integer sortOrder;
    private Integer status;

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getDeptCode() { return deptCode; }
    public void setDeptCode(String deptCode) { this.deptCode = deptCode; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Long getLeaderId() { return leaderId; }
    public void setLeaderId(Long leaderId) { this.leaderId = leaderId; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
