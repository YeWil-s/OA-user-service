package com.oa.datavisual.dto;

import java.math.BigDecimal;

public record ApprovalTrendResponse(
        String statMonth,
        long totalApplications,
        long approvedCount,
        long rejectedCount,
        long pendingCount,
        BigDecimal avgApprovalHours
) {
}
