package com.oa.approval.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ApprovalActionDTO {

    @NotNull(message = "审批结果不能为空")
    private Boolean approved;

    @NotBlank(message = "审批意见不能为空")
    private String comment;

    private Long recipientId;

    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }
}
