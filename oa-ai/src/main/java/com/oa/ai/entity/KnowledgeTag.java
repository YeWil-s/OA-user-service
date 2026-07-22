package com.oa.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.common.base.BaseEntity;

@TableName("ai_knowledge_tag")
public class KnowledgeTag extends BaseEntity {

    private String tagName;
    private String tagCode;
    private String tagDesc;

    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }
    public String getTagCode() { return tagCode; }
    public void setTagCode(String tagCode) { this.tagCode = tagCode; }
    public String getTagDesc() { return tagDesc; }
    public void setTagDesc(String tagDesc) { this.tagDesc = tagDesc; }
}
