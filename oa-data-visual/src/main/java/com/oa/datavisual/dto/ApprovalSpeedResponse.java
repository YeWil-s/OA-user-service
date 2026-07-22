package com.oa.datavisual.dto;

import java.math.BigDecimal;

public record ApprovalSpeedResponse(
        Long deptId,
        String deptName,
        long totalApplications,
        BigDecimal avgApprovalHours
) {
}
