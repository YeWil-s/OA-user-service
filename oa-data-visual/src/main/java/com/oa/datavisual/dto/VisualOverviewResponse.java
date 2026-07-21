package com.oa.datavisual.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VisualOverviewResponse(
        String statMonth,
        long totalEmployees,
        long newHires,
        long resignations,
        BigDecimal attendanceRate,
        BigDecimal overtimeHours,
        long totalApplications,
        long approvedCount,
        long rejectedCount,
        long pendingCount,
        BigDecimal approvalPassRate,
        BigDecimal avgApprovalHours,
        LocalDateTime refreshTime
) {
}
