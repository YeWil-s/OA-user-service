package com.oa.datavisual.dto;

import java.time.LocalDateTime;
import java.util.List;

public record HrScreenResponse(
        String statMonth,
        VisualOverviewResponse overview,
        List<DeptMetricResponse> deptDistribution,
        List<HireResignationTrendResponse> hireResignationTrend,
        List<DeptMetricResponse> deptAttendanceRate,
        LocalDateTime refreshTime
) {
}
