package com.oa.approval.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ApplicationSubmitDTO {

    @NotNull(message = "申请类型不能为空")
    @Min(value = 1, message = "申请类型仅支持1-5")
    @Max(value = 5, message = "申请类型仅支持1-5")
    private Integer appType;

    private Integer leaveType;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long targetDeptId;

    private Long targetPositionId;

    private Long assetId;

    private LocalDate expectReturnDate;

    @NotBlank(message = "申请原因不能为空")
    private String reason;

    private List<String> attachments = new ArrayList<>();

    public Integer getAppType() { return appType; }
    public void setAppType(Integer appType) { this.appType = appType; }
    public Integer getLeaveType() { return leaveType; }
    public void setLeaveType(Integer leaveType) { this.leaveType = leaveType; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Long getTargetDeptId() { return targetDeptId; }
    public void setTargetDeptId(Long targetDeptId) { this.targetDeptId = targetDeptId; }
    public Long getTargetPositionId() { return targetPositionId; }
    public void setTargetPositionId(Long targetPositionId) { this.targetPositionId = targetPositionId; }
    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }
    public LocalDate getExpectReturnDate() { return expectReturnDate; }
    public void setExpectReturnDate(LocalDate expectReturnDate) { this.expectReturnDate = expectReturnDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public List<String> getAttachments() { return attachments; }
    public void setAttachments(List<String> attachments) { this.attachments = attachments; }
}
