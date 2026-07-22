package com.oa.datavisual.dto;

import java.math.BigDecimal;

public record DeptMetricResponse(
        Long deptId,
        String deptName,
        BigDecimal value,
        BigDecimal rate,
        String unit
) {
}
