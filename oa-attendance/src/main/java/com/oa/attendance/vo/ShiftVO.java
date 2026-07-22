package com.oa.attendance.vo;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class ShiftVO {

    private Long id;
    private String shiftName;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalTime flexStart;
    private LocalTime flexEnd;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public LocalTime getFlexStart() { return flexStart; }
    public void setFlexStart(LocalTime flexStart) { this.flexStart = flexStart; }
    public LocalTime getFlexEnd() { return flexEnd; }
    public void setFlexEnd(LocalTime flexEnd) { this.flexEnd = flexEnd; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
