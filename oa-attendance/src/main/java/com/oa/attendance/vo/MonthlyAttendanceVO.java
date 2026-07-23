package com.oa.attendance.vo;

import java.math.BigDecimal;

public class MonthlyAttendanceVO {

    private Long deptId;
    private String deptName;
    private Long totalEmployees;
    private Long normalCount;
    private Long lateCount;
    private Long earlyCount;
    private Long absentCount;
    private Long leaveCount;
    private BigDecimal overtimeTotal;

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public Long getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(Long totalEmployees) { this.totalEmployees = totalEmployees; }
    public Long getNormalCount() { return normalCount; }
    public void setNormalCount(Long normalCount) { this.normalCount = normalCount; }
    public Long getLateCount() { return lateCount; }
    public void setLateCount(Long lateCount) { this.lateCount = lateCount; }
    public Long getEarlyCount() { return earlyCount; }
    public void setEarlyCount(Long earlyCount) { this.earlyCount = earlyCount; }
    public Long getAbsentCount() { return absentCount; }
    public void setAbsentCount(Long absentCount) { this.absentCount = absentCount; }
    public Long getLeaveCount() { return leaveCount; }
    public void setLeaveCount(Long leaveCount) { this.leaveCount = leaveCount; }
    public BigDecimal getOvertimeTotal() { return overtimeTotal; }
    public void setOvertimeTotal(BigDecimal overtimeTotal) { this.overtimeTotal = overtimeTotal; }
}
