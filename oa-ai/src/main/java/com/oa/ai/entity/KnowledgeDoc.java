package com.oa.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.base.BaseEntity;

@TableName("ai_knowledge_doc")
public class KnowledgeDoc extends BaseEntity {

    private String title;
    private String content;
    private String summary;
    private Integer category;
    private String accessRoles;
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
    public String getAccessRoles() { return accessRoles; }
    public void setAccessRoles(String accessRoles) { this.accessRoles = accessRoles; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Long getCreateBy() { return createBy; }
    public void setCreateBy(Long createBy) { this.createBy = createBy; }
}
