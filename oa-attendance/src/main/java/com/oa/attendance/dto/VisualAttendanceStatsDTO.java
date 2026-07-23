package com.oa.attendance.dto;

import java.math.BigDecimal;

public record VisualAttendanceStatsDTO(
        Long deptId,
        int normalCount,
        int lateCount,
        int earlyCount,
        int absentCount,
        int leaveCount,
        BigDecimal overtimeHours
) {
}
