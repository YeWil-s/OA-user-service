package com.oa.datavisual.dto;

import java.time.LocalDateTime;

public record StatisticsSyncResponse(
        String statMonth,
        int deptOverviewRows,
        int attendanceRows,
        int approvalRows,
        LocalDateTime syncTime
) {
}
