package com.oa.attendance.dto;

import java.time.LocalDate;

public record VisualTodayAttendanceDTO(LocalDate date, long punchInCount, long punchOutCount) {
}
