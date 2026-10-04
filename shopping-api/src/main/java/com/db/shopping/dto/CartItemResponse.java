package com.db.shopping.dto;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record CartItemResponse(
        ProductResponse product,
        Integer quantity,
        LocalDateTime expiresAt
) {}