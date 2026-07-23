package com.oa.approval.dto;

import java.math.BigDecimal;

public record VisualApprovalStatsDTO(
        Long deptId,
        int totalApplications,
        int approvedCount,
        int rejectedCount,
        int pendingCount,
        BigDecimal avgApprovalHours
) {
}
