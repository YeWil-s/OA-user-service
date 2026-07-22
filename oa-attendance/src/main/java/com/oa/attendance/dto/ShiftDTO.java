package com.oa.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public class ShiftDTO {

    @NotBlank(message = "班次名称不能为空")
    private String shiftName;

    @NotNull(message = "上班时间不能为空")
    private LocalTime startTime;

    @NotNull(message = "下班时间不能为空")
    private LocalTime endTime;

    private LocalTime flexStart;
    private LocalTime flexEnd;
    private Integer status = 1;

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
}
