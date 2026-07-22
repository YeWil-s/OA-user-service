package com.oa.common.domain;

public enum DataScopeType {

    ALL(0, "全部数据"),
    DEPT_AND_CHILD(1, "本部门及子部门"),
    DEPT(2, "本部门"),
    SELF(3, "仅本人");

    private final int code;
    private final String label;

    DataScopeType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}
