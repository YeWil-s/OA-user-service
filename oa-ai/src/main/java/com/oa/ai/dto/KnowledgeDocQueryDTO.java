package com.oa.ai.dto;

public class KnowledgeDocQueryDTO {

    private String keyword;
    private Long tagId;
    private Long deptId;
    private Long positionId;
    private Long accessDeptId;
    private Integer category;
    private Integer vectorStatus;
    private Integer pageNum = 1;
    private Integer pageSize = 10;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Long getTagId() { return tagId; }
    public void setTagId(Long tagId) { this.tagId = tagId; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public Long getPositionId() { return positionId; }
    public void setPositionId(Long positionId) { this.positionId = positionId; }
    public Long getAccessDeptId() { return accessDeptId; }
    public void setAccessDeptId(Long accessDeptId) { this.accessDeptId = accessDeptId; }
    public Integer getCategory() { return category; }
    public void setCategory(Integer category) { this.category = category; }
    public Integer getVectorStatus() { return vectorStatus; }
    public void setVectorStatus(Integer vectorStatus) { this.vectorStatus = vectorStatus; }
    public Integer getPageNum() { return pageNum; }
    public void setPageNum(Integer pageNum) { this.pageNum = pageNum; }
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
}
