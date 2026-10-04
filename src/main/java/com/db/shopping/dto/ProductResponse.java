package com.db.shopping.dto;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record ProductResponse(
        Long id,
        String sku,
        String name,
        BigDecimal price,
        String currency
) {}