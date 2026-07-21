package com.oa.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.base.BaseEntity;

@TableName("sys_position")
public class SysPosition extends BaseEntity {

    private String positionName;
    private String positionCode;
    private Long deptId;
    private Integer sortOrder;
    private Integer status;

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
}
