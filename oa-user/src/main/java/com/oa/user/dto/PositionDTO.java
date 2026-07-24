package com.oa.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class PositionDTO {

    @NotBlank(message = "岗位名称不能为空")
    private String positionName;
    @NotBlank(message = "岗位编码不能为空")
    private String positionCode;
    @NotNull(message = "所属部门不能为空")
    private Long deptId;
    private Integer sortOrder;
    private Integer status;
    @NotEmpty(message = "岗位必须绑定至少一个角色")
    private List<Long> roleIds;

    public String getPositionName() { return positionName; }
    public void setPositionName(String positionName) { this.positionName = positionName; }
    public String getPositionCode() { return positionCode; }
    public void setPositionCode(String positionCode) { this.positionCode = positionCode; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public List<Long> getRoleIds() { return roleIds; }
    public void setRoleIds(List<Long> roleIds) { this.roleIds = roleIds; }
}
