package com.oa.datavisual.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TodayAttendanceRealtimeResponse(
        LocalDate date,
        long activeEmployees,
        long punchInCount,
        long punchOutCount,
        BigDecimal punchInRate
) {
}
