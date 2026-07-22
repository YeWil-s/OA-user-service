package com.oa.common.result;

public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或Token已过期"),
    FORBIDDEN(403, "无访问权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "业务冲突"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // user-service 10001-10999
    USER_NOT_FOUND(10001, "用户不存在"),
    USER_PASSWORD_ERROR(10002, "密码错误"),
    USER_DISABLED(10003, "账号已被禁用"),
    USER_EXISTED(10004, "用户已存在"),
    DEPT_HAS_CHILDREN(10005, "部门存在子部门，无法删除"),
    DEPT_HAS_USERS(10006, "部门下存在员工，无法删除"),
    ROLE_CODE_DUPLICATE(10007, "角色编码已存在"),
    MENU_HAS_CHILDREN(10008, "菜单存在子菜单，无法删除"),
    POSITION_CODE_DUPLICATE(10009, "岗位编码已存在"),
    OLD_PASSWORD_ERROR(10010, "旧密码错误"),

    // attendance-service 20001-20999
    ALREADY_PUNCHED_IN(20001, "今日已打卡，请勿重复操作"),
    PUNCH_NOT_IN_RANGE(20002, "不在打卡时间范围内"),
    SHIFT_NOT_ASSIGNED(20003, "未分配班次"),

    // approval-service 30001-30999
    APPLICATION_NOT_FOUND(30001, "申请单不存在"),
    CANNOT_CANCEL(30002, "当前状态不可撤销"),
    ALREADY_APPROVED(30003, "该申请已被审批"),

    // notice-service 40001-40999
    NOTICE_NOT_FOUND(40001, "公告不存在"),
    NOTICE_EXPIRED(40002, "公告已过期"),

    // ai-service 50001-50999
    AI_SERVICE_ERROR(50001, "AI服务调用失败"),
    AI_QUOTA_EXCEEDED(50002, "AI调用次数超限"),
    AGENT_INTENT_UNKNOWN(50010, "无法识别意图，请重新描述"),
    AGENT_EXTRACTION_FAILED(50011, "信息提取失败，请补充信息"),
    AGENT_FORM_INCOMPLETE(50012, "表单信息不完整"),
    KNOWLEDGE_NOT_FOUND(50013, "知识文档不存在"),
    EMBEDDING_SERVICE_ERROR(50014, "向量嵌入服务异常"),
    VECTOR_SEARCH_ERROR(50015, "向量检索异常"),
    SESSION_NOT_FOUND(50016, "会话不存在");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
