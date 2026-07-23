package com.oa.approval.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("app_application")
public class AppApplication {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String applicationNo;
    private Long userId;
    private Long deptId;
    private Integer appType;
    private Integer leaveType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal duration;
    private Long targetDeptId;
    private Long targetPositionId;
    private Long assetId;
    private LocalDate expectReturnDate;
    private String reason;
    private String attachments;
    private Integer status;
    private Long currentApproverId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getApplicationNo() { return applicationNo; }
    public void setApplicationNo(String applicationNo) { this.applicationNo = applicationNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public Integer getAppType() { return appType; }
    public void setAppType(Integer appType) { this.appType = appType; }
    public Integer getLeaveType() { return leaveType; }
    public void setLeaveType(Integer leaveType) { this.leaveType = leaveType; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public BigDecimal getDuration() { return duration; }
    public void setDuration(BigDecimal duration) { this.duration = duration; }
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
    public String getAttachments() { return attachments; }
    public void setAttachments(String attachments) { this.attachments = attachments; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Long getCurrentApproverId() { return currentApproverId; }
    public void setCurrentApproverId(Long currentApproverId) { this.currentApproverId = currentApproverId; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
