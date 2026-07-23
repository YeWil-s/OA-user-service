package com.oa.user.dto;

public record VisualDeptStatsDTO(
        Long deptId,
        String deptName,
        int activeEmployees,
        int newHires,
        int resignations
) {
}
