package com.oa.datavisual.dto;

public record HireResignationTrendResponse(
        String statMonth,
        long activeEmployees,
        long newHires,
        long resignations
) {
}
