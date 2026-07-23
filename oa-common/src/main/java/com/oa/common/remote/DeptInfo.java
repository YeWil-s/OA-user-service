package com.oa.common.remote;

import java.util.ArrayList;
import java.util.List;

public class DeptInfo {
    private Long id;
    private Long parentId;
    private String deptName;
    private Long leaderId;
    private List<DeptInfo> children = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public Long getLeaderId() { return leaderId; }
    public void setLeaderId(Long leaderId) { this.leaderId = leaderId; }
    public List<DeptInfo> getChildren() { return children; }
    public void setChildren(List<DeptInfo> children) { this.children = children; }
}
