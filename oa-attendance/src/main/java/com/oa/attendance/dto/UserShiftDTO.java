package com.oa.attendance.dto;

import jakarta.validation.constraints.NotNull;

public class UserShiftDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "班次ID不能为空")
    private Long shiftId;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getShiftId() { return shiftId; }
    public void setShiftId(Long shiftId) { this.shiftId = shiftId; }
}
