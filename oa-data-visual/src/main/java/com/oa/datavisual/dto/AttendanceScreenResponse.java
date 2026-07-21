package com.oa.datavisual.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AttendanceScreenResponse(
        String statMonth,
        VisualOverviewResponse overview,
        List<AttendanceTrendResponse> monthlyTrend,
        List<DeptMetricResponse> deptOvertime,
        List<DeptMetricResponse> deptLateRanking,
        TodayAttendanceRealtimeResponse realtime,
        LocalDateTime refreshTime
) {
}
