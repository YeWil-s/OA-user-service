package com.oa.attendance.vo;

import java.time.LocalDate;
import java.time.LocalTime;

public class ScheduleVO {

    private Long id;
    private Long userId;
    private String userName;
    private String deptName;
    private LocalDate scheduleDate;
    private Long shiftId;
    private String shiftName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer status;
    private String statusText;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public LocalDate getScheduleDate() { return scheduleDate; }
    public void setScheduleDate(LocalDate scheduleDate) { this.scheduleDate = scheduleDate; }
    public Long getShiftId() { return shiftId; }
    public void setShiftId(Long shiftId) { this.shiftId = shiftId; }
    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getStatusText() { return statusText != null ? statusText : (Integer.valueOf(2).equals(status) ? "请假" : "正常"); }
    public void setStatusText(String statusText) { this.statusText = statusText; }
}
