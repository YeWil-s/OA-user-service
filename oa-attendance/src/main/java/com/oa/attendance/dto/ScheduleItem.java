package com.oa.attendance.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class ScheduleItem {

    @NotNull(message = "用户不能为空")
    private Long userId;
    @NotNull(message = "日期不能为空")
    private LocalDate scheduleDate;
    @NotNull(message = "班次不能为空")
    private Long shiftId;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDate getScheduleDate() { return scheduleDate; }
    public void setScheduleDate(LocalDate scheduleDate) { this.scheduleDate = scheduleDate; }
    public Long getShiftId() { return shiftId; }
    public void setShiftId(Long shiftId) { this.shiftId = shiftId; }
}
