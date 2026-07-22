package com.oa.datavisual.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ApprovalScreenResponse(
        String statMonth,
        ApprovalStatsResponse stats,
        List<ApprovalSpeedResponse> speedRanking,
        List<ApprovalTrendResponse> approvalTrend,
        List<NameValueResponse> applicationTypeDistribution,
        long pendingBacklog,
        LocalDateTime refreshTime
) {
}
