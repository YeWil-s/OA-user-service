package com.oa.user.vo;

import java.util.ArrayList;
import java.util.List;

public class DeptTreeVO {

    private Long id;
    private Long parentId;
    private String deptName;
    private String deptCode;
    private Long leaderId;
    private String leaderName;
    private Integer sortOrder;
    private Integer status;
    private List<DeptTreeVO> children = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getDeptCode() { return deptCode; }
    public void setDeptCode(String deptCode) { this.deptCode = deptCode; }
    public Long getLeaderId() { return leaderId; }
    public void setLeaderId(Long leaderId) { this.leaderId = leaderId; }
    public String getLeaderName() { return leaderName; }
    public void setLeaderName(String leaderName) { this.leaderName = leaderName; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public List<DeptTreeVO> getChildren() { return children; }
    public void setChildren(List<DeptTreeVO> children) { this.children = children; }
}
