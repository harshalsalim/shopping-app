package com.db.shopping.dto;

import lombok.Builder;
import java.math.BigDecimal;
import java.util.List;

@Builder
public record CartResponse(
        List<CartItemResponse> items,
        BigDecimal cartTotal
) {}