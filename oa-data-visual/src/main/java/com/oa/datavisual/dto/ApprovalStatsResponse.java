package com.oa.datavisual.dto;

import java.math.BigDecimal;
import java.util.List;

public record ApprovalStatsResponse(
        String statMonth,
        long totalApplications,
        long approvedCount,
        long rejectedCount,
        long pendingCount,
        BigDecimal passRate,
        BigDecimal rejectRate,
        BigDecimal pendingRate,
        List<NameValueResponse> statusDistribution
) {
}
