package com.oa.approval.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ApplicationSubmitDTO {

    @NotNull(message = "申请类型不能为空")
    @Min(value = 1, message = "申请类型只能是1=请假、2=加班、3=外出")
    @Max(value = 3, message = "申请类型只能是1=请假、2=加班、3=外出")
    private Integer appType;

    @Min(value = 1, message = "请假类型只能是1到5")
    @Max(value = 5, message = "请假类型只能是1到5")
    private Integer leaveType;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @NotBlank(message = "申请原因不能为空")
    @Size(max = 500, message = "申请原因不能超过500个字符")
    private String reason;

    @Size(max = 20, message = "附件数量不能超过20个")
    private List<@Size(max = 500, message = "单个附件地址不能超过500个字符") String> attachments = new ArrayList<>();

    public Integer getAppType() { return appType; }
    public void setAppType(Integer appType) { this.appType = appType; }
    public Integer getLeaveType() { return leaveType; }
    public void setLeaveType(Integer leaveType) { this.leaveType = leaveType; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public List<String> getAttachments() { return attachments; }
    public void setAttachments(List<String> attachments) { this.attachments = attachments; }
}
