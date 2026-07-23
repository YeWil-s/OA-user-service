package com.oa.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class KnowledgeDocDTO {

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String summary;

    @NotNull(message = "分类不能为空")
    private Integer category;

    private List<Long> tagIds;
    private Long deptId;
    private List<String> accessRoles;
    private List<Long> accessPositions;
    private List<Long> accessDepts;
    private Integer accessMode;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public Integer getCategory() { return category; }
    public void setCategory(Integer category) { this.category = category; }
    public List<Long> getTagIds() { return tagIds; }
    public void setTagIds(List<Long> tagIds) { this.tagIds = tagIds; }
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
}
