package com.oa.approval.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ApprovalActionDTO {

    @NotNull(message = "审批结果不能为空")
    private Boolean approved;

    @NotBlank(message = "审批意见不能为空")
    @Size(max = 500, message = "审批意见不能超过500个字符")
    private String comment;

    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
