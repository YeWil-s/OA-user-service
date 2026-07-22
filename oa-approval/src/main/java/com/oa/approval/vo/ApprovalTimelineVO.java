package com.oa.approval.vo;

import java.time.LocalDateTime;

public class ApprovalTimelineVO {

    private Long id;
    private Long approverId;
    private String approverName;
    private Integer action;
    private String actionText;
    private String comment;
    private LocalDateTime actionTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }
    public String getApproverName() { return approverName; }
    public void setApproverName(String approverName) { this.approverName = approverName; }
    public Integer getAction() { return action; }
    public void setAction(Integer action) { this.action = action; }
    public String getActionText() { return actionText; }
    public void setActionText(String actionText) { this.actionText = actionText; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getActionTime() { return actionTime; }
    public void setActionTime(LocalDateTime actionTime) { this.actionTime = actionTime; }
}
