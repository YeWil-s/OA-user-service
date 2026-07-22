package com.oa.datavisual.dto;

import java.math.BigDecimal;

public record NameValueResponse(
        String name,
        BigDecimal value,
        String unit
) {
}
