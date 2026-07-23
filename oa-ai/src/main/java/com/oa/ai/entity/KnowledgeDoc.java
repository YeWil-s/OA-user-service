package com.oa.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.base.BaseEntity;

@TableName("ai_knowledge_doc")
public class KnowledgeDoc extends BaseEntity {

    private String title;
    private String content;
    private String summary;
    private Integer category;
    private Long deptId;
    private String accessRoles;
    private String accessPositions;
    private String accessDepts;
    private Integer accessMode;
    private Integer version;
    private Integer vectorStatus;
    private String vectorError;
    private Integer status;
    private Long createBy;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public Integer getCategory() { return category; }
    public void setCategory(Integer category) { this.category = category; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getAccessRoles() { return accessRoles; }
    public void setAccessRoles(String accessRoles) { this.accessRoles = accessRoles; }
    public String getAccessPositions() { return accessPositions; }
    public void setAccessPositions(String accessPositions) { this.accessPositions = accessPositions; }
    public String getAccessDepts() { return accessDepts; }
    public void setAccessDepts(String accessDepts) { this.accessDepts = accessDepts; }
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
    public Long getCreateBy() { return createBy; }
    public void setCreateBy(Long createBy) { this.createBy = createBy; }
}
