package com.oa.ai.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum KnowledgeCategory {

    COMPANY_RULE(1, "公司制度"),
    OPERATION_PROCEDURE(2, "操作流程"),
    HR_POLICY(3, "HR政策"),
    FINANCE_RULE(4, "财务制度"),
    IT_NORM(5, "IT规范"),
    OTHER(6, "其他");

    @EnumValue
    private final int code;
    private final String desc;

    KnowledgeCategory(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }
}
