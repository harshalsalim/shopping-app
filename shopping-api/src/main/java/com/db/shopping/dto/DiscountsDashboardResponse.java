package com.db.shopping.dto;

import lombok.Builder;
import java.math.BigDecimal;
import java.util.List;

@Builder
public record DiscountsDashboardResponse(
        List<DiscountDto> activeDiscounts
) {
    @Builder
    public record DiscountDto(
            String title,
            String code,
            String type,
            BigDecimal value
    ) {}
}