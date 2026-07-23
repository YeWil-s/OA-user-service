package com.oa.approval.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ApplicationVO {

    private Long id;
    private String applicationNo;
    private Long userId;
    private String applicantName;
    private Long deptId;
    private String deptName;
    private Integer appType;
    private String appTypeText;
    private Integer leaveType;
    private String leaveTypeText;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal duration;
    private Long targetDeptId;
    private String targetDeptName;
    private Long targetPositionId;
    private String targetPositionName;
    private Long assetId;
    private String assetName;
    private String assetCode;
    private LocalDate expectReturnDate;
    private String reason;
    private Integer status;
    private String statusText;
    private Long currentApproverId;
    private String currentApproverName;
    private Integer latestAction;
    private String latestActionText;
    private LocalDateTime latestActionTime;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getApplicationNo() { return applicationNo; }
    public void setApplicationNo(String applicationNo) { this.applicationNo = applicationNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public Integer getAppType() { return appType; }
    public void setAppType(Integer appType) { this.appType = appType; }
    public String getAppTypeText() { return appTypeText; }
    public void setAppTypeText(String appTypeText) { this.appTypeText = appTypeText; }
    public Integer getLeaveType() { return leaveType; }
    public void setLeaveType(Integer leaveType) { this.leaveType = leaveType; }
    public String getLeaveTypeText() { return leaveTypeText; }
    public void setLeaveTypeText(String leaveTypeText) { this.leaveTypeText = leaveTypeText; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public BigDecimal getDuration() { return duration; }
    public void setDuration(BigDecimal duration) { this.duration = duration; }
    public Long getTargetDeptId() { return targetDeptId; }
    public void setTargetDeptId(Long targetDeptId) { this.targetDeptId = targetDeptId; }
    public String getTargetDeptName() { return targetDeptName; }
    public void setTargetDeptName(String targetDeptName) { this.targetDeptName = targetDeptName; }
    public Long getTargetPositionId() { return targetPositionId; }
    public void setTargetPositionId(Long targetPositionId) { this.targetPositionId = targetPositionId; }
    public String getTargetPositionName() { return targetPositionName; }
    public void setTargetPositionName(String targetPositionName) { this.targetPositionName = targetPositionName; }
    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }
    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }
    public String getAssetCode() { return assetCode; }
    public void setAssetCode(String assetCode) { this.assetCode = assetCode; }
    public LocalDate getExpectReturnDate() { return expectReturnDate; }
    public void setExpectReturnDate(LocalDate expectReturnDate) { this.expectReturnDate = expectReturnDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }
    public Long getCurrentApproverId() { return currentApproverId; }
    public void setCurrentApproverId(Long currentApproverId) { this.currentApproverId = currentApproverId; }
    public String getCurrentApproverName() { return currentApproverName; }
    public void setCurrentApproverName(String currentApproverName) { this.currentApproverName = currentApproverName; }
    public Integer getLatestAction() { return latestAction; }
    public void setLatestAction(Integer latestAction) { this.latestAction = latestAction; }
    public String getLatestActionText() { return latestActionText; }
    public void setLatestActionText(String latestActionText) { this.latestActionText = latestActionText; }
    public LocalDateTime getLatestActionTime() { return latestActionTime; }
    public void setLatestActionTime(LocalDateTime latestActionTime) { this.latestActionTime = latestActionTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
