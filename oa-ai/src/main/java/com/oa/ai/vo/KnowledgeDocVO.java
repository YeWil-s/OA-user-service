package com.oa.ai.vo;

import java.time.LocalDateTime;
import java.util.List;

public class KnowledgeDocVO {

    private Long id;
    private String title;
    private String summary;
    private Integer category;
    private String categoryDesc;
    private List<String> tagNames;
    private Long deptId;
    private List<String> accessRoles;
    private List<Long> accessPositions;
    private List<Long> accessDepts;
    private Integer accessMode;
    private Integer version;
    private Integer vectorStatus;
    private String vectorError;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public Integer getCategory() { return category; }
    public void setCategory(Integer category) { this.category = category; }
    public String getCategoryDesc() { return categoryDesc; }
    public void setCategoryDesc(String categoryDesc) { this.categoryDesc = categoryDesc; }
    public List<String> getTagNames() { return tagNames; }
    public void setTagNames(List<String> tagNames) { this.tagNames = tagNames; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public List<String> getAccessRoles() { return accessRoles; }
    public void setAccessRoles(List<String> accessRoles) { this.accessRoles = accessRoles; }
    public List<Long> getAccessPositions() { return accessPositions; }
    public void setAccessPositions(List<Long> accessPositions) { this.accessPositions = accessPositions; }
    public List<Long> getAccessDepts() { return accessDepts; }
    public void setAccessDepts(List<Long> accessDepts) { this.accessDepts = accessDepts; }
    public Integer getAccessMode() { return accessMode; }
    public void setAccessMode(Integer accessMode) { this.accessMode = accessMode; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public Integer getVectorStatus() { return vectorStatus; }
    public void setVectorStatus(Integer vectorStatus) { this.vectorStatus = vectorStatus; }
    public String getVectorError() { return vectorError; }
    public void setVectorError(String vectorError) { this.vectorError = vectorError; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
