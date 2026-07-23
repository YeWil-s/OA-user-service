package com.oa.attendance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("att_daily_summary")
public class AttDailySummary {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long deptId;
    private LocalDate summaryDate;
    private Long shiftId;
    private LocalDateTime punchInTime;
    private LocalDateTime punchOutTime;
    private Integer status;
    private Integer lateMinutes;
    private Integer earlyMinutes;
    private BigDecimal workHours;
    private BigDecimal overtimeHours;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public LocalDate getSummaryDate() { return summaryDate; }
    public void setSummaryDate(LocalDate summaryDate) { this.summaryDate = summaryDate; }
    public Long getShiftId() { return shiftId; }
    public void setShiftId(Long shiftId) { this.shiftId = shiftId; }
    public LocalDateTime getPunchInTime() { return punchInTime; }
    public void setPunchInTime(LocalDateTime punchInTime) { this.punchInTime = punchInTime; }
    public LocalDateTime getPunchOutTime() { return punchOutTime; }
    public void setPunchOutTime(LocalDateTime punchOutTime) { this.punchOutTime = punchOutTime; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getLateMinutes() { return lateMinutes; }
    public void setLateMinutes(Integer lateMinutes) { this.lateMinutes = lateMinutes; }
    public Integer getEarlyMinutes() { return earlyMinutes; }
    public void setEarlyMinutes(Integer earlyMinutes) { this.earlyMinutes = earlyMinutes; }
    public BigDecimal getWorkHours() { return workHours; }
    public void setWorkHours(BigDecimal workHours) { this.workHours = workHours; }
    public BigDecimal getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(BigDecimal overtimeHours) { this.overtimeHours = overtimeHours; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
