package com.oa.datavisual.dto;

import java.math.BigDecimal;

public record AttendanceTrendResponse(
        String statMonth,
        long normalCount,
        long lateCount,
        long earlyCount,
        long absentCount,
        long leaveCount,
        BigDecimal overtimeHours,
        BigDecimal attendanceRate
) {
}
