package com.db.shopping.dto;

import java.util.List;

public record CartResponse(
        Long userId,
        List<CartItemDto> items
) {
}
